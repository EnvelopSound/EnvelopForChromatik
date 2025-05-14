/**
 * Envelop for Chromatik
 * Copyright 2025 Mark C. Slee, Envelop
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * @author Mark C. Slee <mcslee@mcslee.com>
 */

package us.envelop.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLayer;
import heronarts.lx.audio.ADM;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.*;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.NormalizedParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/MIDI")
public class NotePattern extends EnvelopPattern {

  private final CompoundParameter attack =
    new CompoundParameter("Attack", 50, 25, 1000)
    .setExponent(2)
    .setUnits(CompoundParameter.Units.MILLISECONDS)
    .setDescription("Sets the attack time of the flash");

  private final CompoundParameter decay =
    new CompoundParameter("Decay", 1000, 50, 10000)
    .setExponent(2)
    .setUnits(CompoundParameter.Units.MILLISECONDS)
    .setDescription("Sets the decay time of the flash");

  private final CompoundParameter size =
    new CompoundParameter("Size", .2)
    .setDescription("Sets the base size of notes");

  private final CompoundParameter pitchBendDepth =
    new CompoundParameter("BendAmt", 0.5)
    .setDescription("Controls the depth of modulation from the Pitch Bend wheel");

  private final CompoundParameter modBrightness =
    new CompoundParameter("Mod>Brt", 0)
    .setDescription("Sets the amount of LFO modulation to note brightness");

  private final CompoundParameter modSize =
    new CompoundParameter("Mod>Sz", 0)
    .setDescription("Sets the amount of LFO modulation to note size");

  private final CompoundParameter lfoRate =
    new CompoundParameter("LFOSpd", 500, 1000, 100)
    .setExponent(2)
    .setDescription("Sets the rate of LFO modulation from the mod wheel");

  private final CompoundParameter velocityBrightness =
    new CompoundParameter("Vel>Brt", .5)
    .setDescription("Sets the amount of modulation from note velocity to brightness");

  private final CompoundParameter velocitySize =
    new CompoundParameter("Vel>Size", .5)
    .setDescription("Sets the amount of modulation from note velocity to size");

  private final CompoundParameter position =
    new CompoundParameter("Pos", .5)
    .setDescription("Sets the base position of middle C");

  private final CompoundParameter pitchDepth =
    new CompoundParameter("Note>Pos", 1, .1, 4)
    .setDescription("Sets the amount pitch modulates the position");

  private final DiscreteParameter soundObject =
    new DiscreteParameter("Object", 0, 17)
    .setDescription("Which sound object to follow");

  private final LXModulator lfo = startModulator(new SinLFO(0, 1, this.lfoRate));

  private float pitchBendValue = 0;
  private float modValue = 0;

  private final NoteLayer[] notes = new NoteLayer[128];

  public NotePattern(LX lx) {
    super(lx);
    for (int i = 0; i < notes.length; ++i) {
      addLayer(this.notes[i] = new NoteLayer(lx, i));
    }
    addParameter("attack", this.attack);
    addParameter("decay", this.decay);
    addParameter("size", this.size);
    addParameter("pitchBendDepth", this.pitchBendDepth);
    addParameter("velocityBrightness", this.velocityBrightness);
    addParameter("velocitySize", this.velocitySize);
    addParameter("modBrightness", this.modBrightness);
    addParameter("modSize", this.modSize);
    addParameter("lfoRate", this.lfoRate);
    addParameter("position", this.position);
    addParameter("pitchDepth", this.pitchDepth);
    addParameter("soundObject", this.soundObject);
  }

  protected class NoteLayer extends LXLayer {

    private final int pitch;

    private float velocity;

    private final FixedParameter zero = new FixedParameter(0);
    private final MutableParameter level = new MutableParameter(0);

    private final AHDSREnvelope envelope = new AHDSREnvelope("Env", zero, attack, zero, decay, zero, zero, zero, level);

    NoteLayer(LX lx, int pitch) {
      super(lx);
      this.pitch = pitch;
      this.envelope.stageMode.setValue(AHDSREnvelope.StageMode.AD);
      addModulator(this.envelope);

    }

    @Override
    public void run(double deltaMs) {
      float pos = position.getValuef() + pitchDepth.getValuef() * (this.pitch - 64) / 64f;
      float level = envelope.getValuef() * (1 - modValue * modBrightness.getValuef() * lfo.getValuef());
      if (level > 0) {
        float yn = pos + pitchBendDepth.getValuef() * pitchBendValue;
        float sz =
          size.getValuef() +
          velocity * velocitySize.getValuef() +
          modValue * modSize.getValuef() * (lfo.getValuef() - .5f);

        NormalizedParameter sourceChannel = null;
        ADM.Obj sourceObject = null;
        float tx = model.cx, tz = model.cz;

        int soundObjectIndex = soundObject.getValuei();
        if (soundObjectIndex > 0) {
          sourceChannel = lx.engine.audio.envelop.source.channels[soundObjectIndex - 1];
          sourceObject = lx.engine.audio.adm.obj.get(soundObjectIndex - 1);
          tx = LXUtils.lerpf(model.xMin, model.xMax, sourceObject.x.getNormalizedf());
          tz = LXUtils.lerpf(model.zMin, model.zMax, sourceObject.y.getNormalizedf());
        }


        float falloff = 50.f / sz;
        for (LXModel rail : getRails()) {
          float l2 = level;
          if (sourceChannel != null) {
            float l2fall = 100 / (20*FEET);
            l2 = level - l2fall * LXUtils.maxf(0, LXUtils.distf(tx, tz, rail.cx, rail.cz) - 2*FEET);
          }
          for (LXPoint p : rail.points) {
            float b = l2 - falloff * Math.abs(p.yn - yn);
            if (b > 0) {
              addColor(p.index, LXColor.gray(b));
            }
          }
        }
      }
    }
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    NoteLayer noteLayer = this.notes[note.getPitch()];
    noteLayer.velocity = note.getVelocity() / 127f;
    noteLayer.level.setValue(LXUtils.lerpf(100.f, noteLayer.velocity * 100, this.velocityBrightness.getNormalizedf()));
    noteLayer.envelope.engage.setValue(true);
  }

  @Override
  public void noteOffReceived(MidiNote note) {
    this.notes[note.getPitch()].envelope.engage.setValue(false);
  }

  @Override
  public void pitchBendReceived(MidiPitchBend pb) {
    this.pitchBendValue = (float) pb.getNormalized();
  }

  @Override
  public void controlChangeReceived(MidiControlChange cc) {
    if (cc.getCC() == MidiControlChange.MOD_WHEEL) {
      this.modValue = (float) cc.getNormalized();
    }
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }
}
