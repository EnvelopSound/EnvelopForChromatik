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
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Texture")
public final class Swarm extends EnvelopPattern {

  private static final double MIN_PERIOD = 200;

  public final CompoundParameter chunkSize =
    new CompoundParameter("Chunk", 10, 5, 20)
    .setDescription("Size of the swarm chunks");

  private final LXParameter chunkDamped = startModulator(new DampedParameter(this.chunkSize, 5, 5));

  public final CompoundParameter speed =
    new CompoundParameter("Speed", .5, .01, 1)
    .setDescription("Speed of the swarm motion");

  public final CompoundParameter oscillation =
    new CompoundParameter("Osc", 0)
    .setDescription("Amoount of oscillation of the swarm speed");

  private final FunctionalParameter minPeriod = new FunctionalParameter() {
    @Override
    public double getValue() {
      return MIN_PERIOD / speed.getValue();
    }
  };

  private final FunctionalParameter maxPeriod = new FunctionalParameter() {
    @Override
    public double getValue() {
      return MIN_PERIOD / (speed.getValue() + oscillation.getValue());
    }
  };

  private final SawLFO pos = new SawLFO(0, 1, startModulator(
    new SinLFO(minPeriod, maxPeriod, startModulator(
      new SinLFO(9000, 23000, 49000).randomBasis()
  )).randomBasis()));

  private final SinLFO swarmA = new SinLFO(0, 4*LX.PIf, startModulator(
    new SinLFO(37000, 79000, 51000)
  ));

  private final SinLFO swarmY = new SinLFO(
    startModulator(new SinLFO(model.yMin, model.cy, 19000).randomBasis()),
    startModulator(new SinLFO(model.cy, model.yMax, 23000).randomBasis()),
    startModulator(new SinLFO(14000, 37000, 19000))
  );

  private final SinLFO swarmSize = new SinLFO(.6, 1, startModulator(
    new SinLFO(7000, 19000, 11000)
  ));

  public final CompoundParameter size =
    new CompoundParameter("Size", 1, 2, .5)
    .setDescription("Size of the overall swarm");

  public Swarm(LX lx) {
    super(lx);
    addParameter("chunk", this.chunkSize);
    addParameter("size", this.size);
    addParameter("speed", this.speed);
    addParameter("oscillation", this.oscillation);
    startModulator(this.pos.randomBasis());
    startModulator(this.swarmA);
    startModulator(this.swarmY);
    startModulator(this.swarmSize);
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
    float chunkSize = this.chunkDamped.getValuef();
    float pos = this.pos.getValuef();
    float swarmA = this.swarmA.getValuef();
    float swarmY = this.swarmY.getValuef();
    float swarmSize = this.swarmSize.getValuef() * this.size.getValuef();

    int columnIndex = 0;
    for (LXModel column : getColumns()) {
      int ri = 0;
      for (LXModel rail : column.sub("rail")) {
        for (int i = 0; i < rail.points.length; ++i) {
          LXPoint p = rail.points[i];
          float f = (i % chunkSize) / chunkSize;
          if ((columnIndex + ri) % 3 == 2) {
            f = 1-f;
          }
          float fd = 40*LXUtils.wrapdistf(column.azimuth, swarmA, LX.TWO_PIf) + Math.abs(p.y - swarmY);
          fd *= swarmSize;
          colors[p.index] = LXColor.gray(LXUtils.maxf(0, 100 - fd - (100 + fd) * LXUtils.wrapdistf(f, pos, 1)));
        }
        ++ri;
      }
      ++columnIndex;
    }
  }
}
