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
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Texture")
public class Jitters extends EnvelopPattern {

  public final CompoundParameter period = new CompoundParameter("Period", 200, 2000, 50)
  .setExponent(.5)
  .setDescription("Speed of the motion");

  public final CompoundParameter size =
    new CompoundParameter("Size", 8, 3, 20)
    .setDescription("Size of the movers");

  public final CompoundParameter contrast =
    new CompoundParameter("Contrast", 100, 50, 300)
    .setDescription("Amount of contrast");

  final LXModulator pos = startModulator(new SawLFO(0, 1, period));

  final LXModulator sizeDamped = startModulator(new DampedParameter(size, 30));

  public Jitters(LX lx) {
    super(lx);
    addParameter("period", this.period);
    addParameter("size", this.size);
    addParameter("contrast", this.contrast);
  }

  @Override
  public void run(double deltaMs) {
    float size = this.sizeDamped.getValuef();
    float pos = this.pos.getValuef();
    float sizeInv = 1 / size;
    float contrast = this.contrast.getValuef();
    boolean inv = false;
    for (LXModel rail : getRails()) {
      inv = !inv;
      float pv = inv ? pos : (1-pos);
      int i = 0;
      for (LXPoint p : rail.points) {
        float pd = (i % size) * sizeInv;
        colors[p.index] = LXColor.gray(LXUtils.maxf(0, 100 - contrast * LXUtils.wrapdistf(pd, pv, 1)));
        ++i;
      }
    }
  }
}
