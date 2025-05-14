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

import java.util.Stack;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelope/MIDI")
public class Blips extends EnvelopPattern {

  public final CompoundParameter speed = new CompoundParameter("Speed", 500, 4000, 250);

  final Stack<Blip> available = new Stack<Blip>();

  public Blips(LX lx) {
    super(lx);
    addParameter("speed", this.speed);
  }

  class Blip extends LXLayer {

    public final LinearEnvelope dist = new LinearEnvelope(0, model.yRange, new FunctionalParameter() {
      @Override
      public double getValue() {
        return speed.getValue() * LXUtils.lerp(1, .6, velocity);
      }
    });

    private float yStart;
    private LXModel column;
    private boolean active = false;
    private float velocity = 0;

    public Blip(LX lx) {
      super(lx);
      addModulator(this.dist);
    }

    public void trigger(MidiNoteOn note) {
      this.velocity = note.getVelocity() / 127f;
      this.column = getColumns().get(note.getPitch() % getColumns().size());
      this.yStart = model.cy + LXUtils.randomf(-2*FEET, 2*FEET);
      this.dist.trigger();
      this.active = true;
    }

    @Override
    public void run(double deltaMs) {
      if (!this.active) {
        return;
      }
      boolean touched = false;
      float dist = this.dist.getValuef();
      float falloff = 100 / (1*FEET);
      float level = LXUtils.lerpf(50, 100, this.velocity);
      for (LXModel rail : this.column.sub("rail")) {
        for (LXPoint p : rail.points) {
          float b = level - falloff * Math.abs(Math.abs(p.y - this.yStart) - dist);
          if (b > 0) {
            touched = true;
            addColor(p.index, LXColor.gray(b));
          }
        }
      }
      if (!touched) {
        this.active = false;
        available.push(this);
      }
    }
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    // TODO(mcslee): hack to not fight with flash
    if (note.getPitch() == 72) {
      return;
    }

    Blip blip;
    if (available.empty()) {
      addLayer(blip = new Blip(lx));
    } else {
      blip = available.pop();
    }
    blip.trigger(note);
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }
}