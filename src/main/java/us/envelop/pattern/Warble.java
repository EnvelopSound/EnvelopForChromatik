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
public class Warble extends RotationPattern {

  private final CompoundParameter size = new CompoundParameter("Size", 2*FEET, 6*INCHES, 12*FEET)
  .setDescription("Size of the warble");

  private final CompoundParameter depth = new CompoundParameter("Depth", .4, 0, 1)
  .setExponent(2)
  .setDescription("Depth of the modulation");

  private final CompoundParameter interp =
    new CompoundParameter("Interp", 1, 1, 3)
    .setDescription("Interpolation on the warble");

  private final DampedParameter interpDamped = new DampedParameter(interp, .5, .5);
  private final DampedParameter depthDamped = new DampedParameter(depth, .4, .4);

  public Warble(LX lx) {
    super(lx);
    startModulator(this.interpDamped);
    startModulator(this.depthDamped);
    addParameter("size", this.size);
    addParameter("interp", this.interp);
    addParameter("depth", this.depth);
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
    float phaseV = this.phase.getValuef();
    float interpV = this.interpDamped.getValuef();
    int mult = (int) Math.floor(interpV);
    float lerp = interpV % mult;
    float falloff = 200 / size.getValuef();
    float depth = this.depthDamped.getValuef();
    for (LXModel rail : getRails()) {
      float y1 = model.yRange * depth * (float) Math.sin(phaseV + mult * rail.azimuth);
      float y2 = model.yRange * depth * (float) Math.sin(phaseV + (mult+1) * rail.azimuth);
      float yo = LXUtils.lerpf(y1, y2, lerp);
      for (LXPoint p : rail.points) {
        colors[p.index] = LXColor.gray(LXUtils.maxf(0, 100 - falloff*Math.abs(p.y - model.cy - yo)));
      }
    }
  }
}