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

import java.util.List;
import java.util.Stack;

import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UIDoubleBox;
import heronarts.glx.ui.component.UIKnob;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;

@LXCategory("Envelop/Form")
public class Raindrops extends EnvelopPattern implements UIDeviceControls<Raindrops> {

  private static final float MAX_VEL = -180;

  private final Stack<Drop> availableDrops = new Stack<Drop>();

  public final CompoundParameter velocity = new CompoundParameter("Velocity", 0, MAX_VEL)
  .setDescription("Initial velocity of drops");

  public final CompoundParameter randomVelocity =
    new CompoundParameter("Rnd>Vel", 0, MAX_VEL)
    .setDescription("How much to randomize initial velocity of drops");

  public final CompoundParameter gravity = new CompoundParameter("Gravity", -386, -1, -500)
  .setExponent(3)
  .setDescription("Gravity rate for drops to fall");

  public final CompoundParameter size = new CompoundParameter("Size", 4*INCHES, 1*INCHES, 48*INCHES)
  .setExponent(2)
  .setDescription("Size of the raindrops");

  public final CompoundParameter randomSize = new CompoundParameter("Rnd>Sz", 1*INCHES, 0, 48*INCHES)
  .setExponent(2)
  .setDescription("Amount of size randomization");

  public final CompoundParameter negative =
    new CompoundParameter("Negative", 0)
    .setDescription("Whether drops are light or dark");

  public final BooleanParameter reverse =
    new BooleanParameter("Reverse", false)
    .setDescription("Whether drops fall from the ground to the sky");

  public final BooleanParameter auto =
    new BooleanParameter("Auto", false)
    .setDescription("Whether drops automatically fall");

  public final CompoundParameter rate =
    new CompoundParameter("Rate", .5, 30)
    .setDescription("Rate at which new drops automatically fall");

  private final Click click = new Click("click", new FunctionalParameter() {
    @Override
    public double getValue() {
      return 1000 / rate.getValue();
    }
  });

  public Raindrops(LX lx) {
    super(lx);
    addParameter("velocity", this.velocity);
    addParameter("randomVelocity", this.randomVelocity);
    addParameter("gravity", this.gravity);
    addParameter("size", this.size);
    addParameter("randomSize", this.randomSize);
    addParameter("negative", this.negative);
    addParameter("auto", this.auto);
    addParameter("rate", this.rate);
    addParameter("reverse", this.reverse);
    startModulator(click);
  }

  private void triggerDrop() {
    if (availableDrops.empty()) {
      Drop drop = new Drop(lx);
      addLayer(drop);
      availableDrops.push(drop);
    }
    availableDrops.pop().initialize();
  }

  private class Drop extends LXLayer {

    private final Accelerator accel = new Accelerator(model.yMax, velocity, gravity);
    private float random;

    private LXModel rail;
    private boolean active = false;

    Drop(LX lx) {
      super(lx);
      addModulator(this.accel);
    }

    void initialize() {
      final List<LXModel> rails = getRails();
      int railIndex = (int) Math.round(Math.random() * (rails.size()-1));
      this.rail = rails.get(railIndex);
      this.random = (float) Math.random();
      this.accel.reset();
      this.accel.setVelocity(this.accel.getVelocity() + Math.random() * randomVelocity.getValue());
      this.accel.setValue(model.yMax + size.getValuef() + this.random * randomSize.getValuef()).start();
      this.active = true;
    }

    @Override
    public void run(double deltaMs) {
      if (this.active) {
        float len = size.getValuef() + this.random * randomSize.getValuef();
        float falloff = 100 / len;
        float accel = this.accel.getValuef();
        float pos = reverse.isOn() ? (model.yMin + model.yMax - accel) : accel;
        for (LXPoint p : this.rail.points) {
          float b = 100 - falloff * Math.abs(p.y - pos);
          if (b > 0) {
            addColor(p.index, LXColor.gray(b));
          }
        }
        if (accel < -len) {
          this.active = false;
          availableDrops.push(this);
        }
      }
    }
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    triggerDrop();
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
    if (this.click.click() && this.auto.isOn()) {
      triggerDrop();
    }
  }

  @Override
  public void afterLayers(double deltaMs) {
    float neg = this.negative.getValuef();
    if (neg > 0) {
      for (LXModel rail : getRails()) {
        for (LXPoint p : rail.points) {
          colors[p.index] = LXColor.lerp(colors[p.index], LXColor.subtract(LXColor.WHITE, colors[p.index]), neg);
        }
      }
    }
  }

  @Override
  public void buildDeviceControls(UI ui, UIDevice device, Raindrops raindrops) {
    device.setLayout(UI2dContainer.Layout.VERTICAL);
    device.setChildSpacing(6);
    new UIKnob(this.velocity).addToContainer(device);
    new UIKnob(this.gravity).addToContainer(device);
    new UIKnob(this.size).addToContainer(device);
    new UIDoubleBox(0, 0, device.getContentWidth(), 16)
      .setParameter(this.randomVelocity)
      .addToContainer(device);
    new UIButton(0, 0, device.getContentWidth(), 16)
      .setParameter(this.auto)
      .setLabel("Auto")
      .addToContainer(device);
    new UIDoubleBox(0, 0, device.getContentWidth(), 16)
      .setParameter(this.rate)
      .addToContainer(device);

  }
}