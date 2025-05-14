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

import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UIIntegerBox;
import heronarts.glx.ui.component.UIKnob;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.modulator.AHDSREnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/MIDI")
public class Flash extends EnvelopPattern implements UIDeviceControls<Flash> {

  private final BooleanParameter manual =
    new BooleanParameter("Trigger")
    .setMode(BooleanParameter.Mode.MOMENTARY)
    .setDescription("Manually triggers the flash");

  private final BooleanParameter midi =
    new BooleanParameter("MIDI", true)
    .setDescription("Toggles whether the flash is engaged by MIDI note events");

  private final BooleanParameter midiNoteFilter =
    new BooleanParameter("Note Filter")
    .setDescription("Whether to filter specific MIDI note");

  private final DiscreteParameter midiNote = new DiscreteParameter("Note", 0, 128)
  .setUnits(LXParameter.Units.MIDI_NOTE)
  .setDescription("Note to filter for");

  private final CompoundParameter brightness =
    new CompoundParameter("Brt", 100, 0, 100)
    .setDescription("Sets the maxiumum brightness of the flash");

  private final CompoundParameter velocitySensitivity =
    new CompoundParameter("Vel>Brt", .5)
    .setDescription("Sets the amount to which brightness responds to note velocity");

  private final CompoundParameter attack = new CompoundParameter("Attack", 50, 25, 1000)
  .setExponent(2)
  .setUnits(LXParameter.Units.MILLISECONDS)
  .setDescription("Sets the attack time of the flash");

  private final CompoundParameter decay = new CompoundParameter("Decay", 1000, 50, 10000)
  .setExponent(2)
  .setUnits(LXParameter.Units.MILLISECONDS)
  .setDescription("Sets the decay time of the flash");

  private final CompoundParameter shape = new CompoundParameter("Shape", 1, 1, 4)
  .setDescription("Sets the shape of the attack and decay curves");

  private final FixedParameter zero = new FixedParameter(0);
  private final MutableParameter level = new MutableParameter(0);

  private final AHDSREnvelope env = new AHDSREnvelope("Env", zero, attack, zero, decay, zero, zero, zero, level);

  public Flash(LX lx) {
    super(lx);
    this.env.setShape(shape);
    addModulator(this.env);
    addParameter("brightness", this.brightness);
    addParameter("attack", this.attack);
    addParameter("decay", this.decay);
    addParameter("shape", this.shape);
    addParameter("velocitySensitivity", this.velocitySensitivity);
    addParameter("manual", this.manual);
    addParameter("midi", this.midi);
    addParameter("midiNoteFilter", this.midiNoteFilter);
    addParameter("midiNote", this.midiNote);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if (p == this.manual) {
      if (this.manual.isOn()) {
        level.setValue(brightness.getValue());
      }
      this.env.engage.setValue(this.manual.isOn());
    }
  }

  private boolean isValidNote(MidiNote note) {
    return this.midi.isOn() && (!this.midiNoteFilter.isOn() || (note.getPitch() == this.midiNote.getValuei()));
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    if (isValidNote(note)) {
      level.setValue(brightness.getValue() * LXUtils.lerpf(1, note.getVelocity() / 127f, velocitySensitivity.getValuef()));
      this.env.engage.setValue(true);
    }
  }

  @Override
  public void noteOffReceived(MidiNote note) {
    if (isValidNote(note)) {
      this.env.engage.setValue(false);
    }
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.gray(env.getValue()));
  }

  @Override
  public void buildDeviceControls(UI ui, UIDevice device, Flash flash) {
    device.setContentWidth(216);
    new UIADWave(ui, 0, 0, device.getContentWidth(), 90).addToContainer(device);

    new UIButton(0, 92, 84, 16).setLabel("Trigger").setParameter(this.manual).setTriggerable(true).addToContainer(device);

    new UIButton(88, 92, 40, 16).setParameter(this.midi).setLabel("Midi").addToContainer(device);

    final UIButton midiFilterButton = (UIButton)
      new UIButton(132, 92, 40, 16)
      .setParameter(this.midiNoteFilter)
      .setLabel("Note")
      .setEnabled(this.midi.isOn())
      .addToContainer(device);

    final UIIntegerBox midiNoteBox = (UIIntegerBox)
      new UIIntegerBox(176, 92, 40, 16)
      .setParameter(this.midiNote)
      .setEnabled(this.midi.isOn() && this.midiNoteFilter.isOn())
      .addToContainer(device);

    new UIKnob(0, 116).setParameter(this.brightness).addToContainer(device);
    new UIKnob(44, 116).setParameter(this.attack).addToContainer(device);
    new UIKnob(88, 116).setParameter(this.decay).addToContainer(device);
    new UIKnob(132, 116).setParameter(this.shape).addToContainer(device);

    final UIKnob velocityKnob = (UIKnob)
      new UIKnob(176, 116)
      .setParameter(this.velocitySensitivity)
      .setEnabled(this.midi.isOn())
      .addToContainer(device);

    device.addListener(this.midi, p -> {
      velocityKnob.setEnabled(midi.isOn());
      midiFilterButton.setEnabled(midi.isOn());
      midiNoteBox.setEnabled(midi.isOn() && midiNoteFilter.isOn());
    });

    device.addListener(this.midiFilter, p -> {
      midiNoteBox.setEnabled(midi.isOn() && midiNoteFilter.isOn());
    });
  }

  class UIADWave extends UI2dComponent {
    UIADWave(UI ui, float x, float y, float w, float h) {
      super(x, y, w, h);
      setBackgroundColor(ui.theme.paneBackgroundColor);
      setBorderColor(ui.theme.controlBorderColor);

      brightness.addListener(this.redraw);
      attack.addListener(this.redraw);
      decay.addListener(this.redraw);
      shape.addListener(this.redraw);
    }

    public void onDraw(UI ui, VGraphics vg) {
      double av = attack.getValue();
      double dv = decay.getValue();
      double tv = av + dv;
      double ax = av/tv * (this.width-1);
      double bv = brightness.getValue() / 100.;

      vg.strokeColor(ui.theme.primaryColor);
      vg.beginPath();
      int py = 0;
      for (int x = 1; x < this.width-2; ++x) {
        int y = (x < ax) ?
          (int) Math.round(bv * (height-4.) * Math.pow(((x-1) / ax), shape.getValue())) :
          (int) Math.round(bv * (height-4.) * Math.pow(1 - ((x-ax) / (this.width-1-ax)), shape.getValue()));
        if (x > 1) {
          vg.line(x-1, height-2-py, x, height-2-y);
        }
        py = y;
      }
      vg.stroke();
    }
  }


}