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
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Form")
public class Helix extends RotationPattern {

  private final CompoundParameter size = new CompoundParameter("Size", 2*FEET, 6*INCHES, 8*FEET)
  .setDescription("Size of the corkskrew");

  private final CompoundParameter coil = new CompoundParameter("Coil", 1, .25, 2.5)
  .setExponent(.5)
  .setDescription("Coil amount");

  private final DampedParameter dampedCoil = new DampedParameter(coil, .2);

  public Helix(LX lx) {
    super(lx);
    addParameter("size", this.size);
    addParameter("coil", this.coil);
    startModulator(dampedCoil);
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
    float phaseV = this.phase.getValuef();
    float sizeV = this.size.getValuef();
    float falloff = 200 / sizeV;
    float coil = this.dampedCoil.getValuef();

    for (LXModel rail : getRails()) {
      float yp = -sizeV + ((phaseV + (LX.PIf + coil * rail.azimuth)) % LX.TWO_PIf) / LX.TWO_PIf * (model.yRange + 2*sizeV);
      float yp2 = -sizeV + ((phaseV + coil * rail.azimuth) % LX.TWO_PIf) / LX.TWO_PIf * (model.yRange + 2*sizeV);
      for (LXPoint p : rail.points) {
        float d1 = 100 - falloff*Math.abs(p.y - yp);
        float d2 = 100 - falloff*Math.abs(p.y - yp2);
        float b = LXUtils.maxf(d1, d2);
        colors[p.index] = b > 0 ? LXColor.gray(b) : LXColor.BLACK;
      }
    }
  }
}