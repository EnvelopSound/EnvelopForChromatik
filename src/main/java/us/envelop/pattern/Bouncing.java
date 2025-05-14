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
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Form")
public class Bouncing extends EnvelopPattern {

  public CompoundParameter gravity = new CompoundParameter("Gravity", -200, -10, -400)
  .setExponent(2)
  .setDescription("Gravity factor");

  public CompoundParameter size =
    new CompoundParameter("Length", 2*FEET, 1*FEET, 8*FEET)
    .setDescription("Length of the bouncers");

  public CompoundParameter amp =
    new CompoundParameter("Height", model.yRange, 1*FEET, model.yRange)
    .setDescription("Height of the bounce");

  public Bouncing(LX lx) {
    super(lx);
    addParameter("gravity", this.gravity);
    addParameter("size", this.size);
    addParameter("amp", this.amp);
    for (LXModel column : getColumns()) {
      addLayer(new Bouncer(lx, column));
    }
  }

  class Bouncer extends LXLayer {

    private final LXModel column;
    private final Accelerator position;

    Bouncer(LX lx, LXModel column) {
      super(lx);
      this.column = column;
      this.position = new Accelerator(column.yMax, 0, gravity);
      startModulator(position);
    }

    @Override
    public void run(double deltaMs) {
      if (position.getValue() < 0) {
        position.setValue(-position.getValue());
        position.setVelocity(Math.sqrt(Math.abs(2 * (amp.getValuef() - LXUtils.random(0, 2*FEET)) * gravity.getValuef())));
      }
      float falloff = 100f / size.getValuef();
      for (LXModel rail : column.sub("rail")) {
        for (LXPoint p : rail.points) {
          float b = 100 - falloff * Math.abs(p.y - position.getValuef());
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