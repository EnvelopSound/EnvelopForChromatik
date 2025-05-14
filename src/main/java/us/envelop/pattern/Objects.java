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

import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UIKnob;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXLayer;
import heronarts.lx.audio.ADM;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Meters")
public class Objects extends EnvelopPattern implements UIDeviceControls<Objects> {

  public static final int MAX_OBJECTS = 16;

  public final CompoundParameter size = new CompoundParameter("Base", 4*FEET, 0, 24*FEET);
  public final BoundedParameter response = new BoundedParameter("Level", 0, 1*FEET, 24*FEET);
  public final CompoundParameter spread = new CompoundParameter("Spread", 1, 1, .2);

  public Objects(LX lx) {
    super(lx);
    addParameter("size", this.size);
    addParameter("response", this.response);
    addParameter("spread", this.spread);
    for (int i = 0; i < MAX_OBJECTS; ++i) {
      Layer layer = new Layer(lx, i);
      addLayer(layer);
      addParameter("active-" + (i+1), layer.active);
    }
  }

  @Override
  public void buildDeviceControls(UI ui, UIDevice device, Objects objects) {
    int i = 0;
    for (LXLayer layer : getLayers()) {
      new UIButton((i % 4)*33, (i/4)*28, 28, 24)
      .setLabel(Integer.toString(i+1))
      .setParameter(((Layer) layer).active)
      .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE)
      .addToContainer(device);
      ++i;
    }
    int knobSpacing = UIKnob.WIDTH + 4;
    new UIKnob(0, 116).setParameter(this.size).addToContainer(device);
    new UIKnob(knobSpacing, 116).setParameter(this.response).addToContainer(device);
    new UIKnob(2*knobSpacing, 116).setParameter(this.spread).addToContainer(device);

    device.setContentWidth(3*knobSpacing - 4);
  }

  class Layer extends LXLayer {

    private final int objectIndex;
    private final BooleanParameter active = new BooleanParameter("Active", true);

    private final MutableParameter tx = new MutableParameter();
    private final MutableParameter ty = new MutableParameter();
    private final MutableParameter tz = new MutableParameter();
    private final DampedParameter x = new DampedParameter(this.tx, 50*FEET);
    private final DampedParameter y = new DampedParameter(this.ty, 50*FEET);
    private final DampedParameter z = new DampedParameter(this.tz, 50*FEET);

    Layer(LX lx, int objectIndex) {
      super(lx);
      this.objectIndex = objectIndex;
      startModulator(this.x);
      startModulator(this.y);
      startModulator(this.z);
    }

    @Override
    public void run(double deltaMs) {
      if (!this.active.isOn()) {
        return;
      }

      final ADM.Obj object = this.lx.engine.audio.adm.obj.get(this.objectIndex);
      final float level = this.lx.engine.audio.envelop.source.channels[this.objectIndex].getValuef();

      // Note: ADM object space is x[Left -> Right], y[Back -> Front], z[Down -> Up]
      this.tx.setValue(LXUtils.lerp(model.xMin, model.xMax, object.x.getNormalized()));
      this.ty.setValue(LXUtils.lerp(model.yMin, model.yMax, object.z.getNormalized()));
      this.tz.setValue(LXUtils.lerp(model.zMin, model.zMax, object.y.getNormalized()));

      final float x = this.x.getValuef();
      final float y = this.y.getValuef();
      final float z = this.z.getValuef();
      float spreadf = spread.getValuef();
      float falloff = 100 / (size.getValuef() + response.getValuef() * level);
      for (LXModel rail : getRails()) {
        for (LXPoint p : rail.points) {
          float dist = LXUtils.distf(p.x * spreadf, p.y, p.z * spreadf, x * spreadf, y, z * spreadf);
          float b = 100 - dist*falloff;
          if (b > 0) {
            addColor(p.index, LXColor.gray(b));
          }
        }
      }
    }
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }
}