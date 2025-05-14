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
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Form")
public class Tron extends EnvelopPattern {

  private final static int MIN_DENSITY = 5;
  private final static int MAX_DENSITY = 80;

  private CompoundParameter period = new CompoundParameter("Speed", 150000, 400000, 50000)
  .setExponent(.5)
  .setDescription("Speed of movement");

  private CompoundParameter size = new CompoundParameter("Size", 2*FEET, 6*INCHES, 5*FEET)
  .setExponent(2)
  .setDescription("Size of strips");

  private CompoundParameter density = new CompoundParameter("Density", 25, MIN_DENSITY, MAX_DENSITY)
  .setDescription("Density of tron strips");

  public Tron(LX lx) {
    super(lx);
    addParameter("period", this.period);
    addParameter("size", this.size);
    addParameter("density", this.density);
    for (int i = 0; i < MAX_DENSITY; ++i) {
      addLayer(new Mover(lx, i));
    }
  }

  class Mover extends LXLayer {

    final int index;

    final TriangleLFO pos = new TriangleLFO(0, model.size, period);

    private final MutableParameter targetBrightness = new MutableParameter(100);

    private final DampedParameter brightness = new DampedParameter(this.targetBrightness, 50);

    Mover(LX lx, int index) {
      super(lx);
      this.index = index;
      startModulator(this.brightness);
      startModulator(this.pos.randomBasis());
    }

    @Override
    public void run(double deltaMs) {
      this.targetBrightness.setValue((density.getValuef() > this.index) ? 100 : 0);
      float maxb = this.brightness.getValuef();
      if (maxb > 0) {
        float pos = this.pos.getValuef();
        float falloff = maxb / size.getValuef();
        for (LXPoint p : model.points) {
          float b = maxb - falloff * LXUtils.wrapdistf(p.index, pos, model.points.length);
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