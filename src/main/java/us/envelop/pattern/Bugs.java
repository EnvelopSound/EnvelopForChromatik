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
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.utils.LXUtils;

public class Bugs extends EnvelopPattern {

  public final CompoundParameter speed = new CompoundParameter("Speed", 10, 20, 1)
  .setDescription("Speed of the bugs");

  public final CompoundParameter size =
    new CompoundParameter("Size", .1, .02, .4)
    .setDescription("Size of the bugs");

  public Bugs(LX lx) {
    super(lx);
    for (LXModel rail : getRails()) {
      for (int i = 0; i < 10; ++i) {
        addLayer(new Layer(lx, rail));
      }
    }
    addParameter("speed", this.speed);
    addParameter("size", this.size);
  }

  class RandomSpeed extends FunctionalParameter {

    private final float rand;

    RandomSpeed(float low, float hi) {
      this.rand = LXUtils.randomf(low, hi);
    }

    @Override
    public double getValue() {
      return this.rand * speed.getValue();
    }
  }

  class Layer extends LXLayer {

    private final LXModel rail;
    private final LXModulator pos = startModulator(new SinLFO(
      startModulator(new SinLFO(0, .5, new RandomSpeed(500, 1000)).randomBasis()),
      startModulator(new SinLFO(.5, 1, new RandomSpeed(500, 1000)).randomBasis()),
      new RandomSpeed(3000, 8000)
    ).randomBasis());

    private final LXModulator size = startModulator(new SinLFO(
      startModulator(new SinLFO(.1, .3, new RandomSpeed(500, 1000)).randomBasis()),
      startModulator(new SinLFO(.5, 1, new RandomSpeed(500, 1000)).randomBasis()),
      startModulator(new SinLFO(4000, 14000, LXUtils.random(3000, 18000)).randomBasis())
    ).randomBasis());

    Layer(LX lx, LXModel rail) {
      super(lx);
      this.rail = rail;
    }

    @Override
    public void run(double deltaMs) {
      float size = Bugs.this.size.getValuef() * this.size.getValuef();
      float falloff = 100 / LXUtils.maxf(size, (1.5f * INCHES / model.yRange));
      float pos = this.pos.getValuef();
      for (LXPoint p : this.rail.points) {
        float b = 100 - falloff * Math.abs(p.yn - pos);
        if (b > 0) {
          addColor(p.index, LXColor.gray(b));
        }
      }
    }
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }
}
