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

package us.envelop.effect;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Texture")
public class Sizzle extends LXEffect {

  public final CompoundParameter amount =
    new CompoundParameter("Amount", .5)
    .setDescription("Intensity of the effect");

  public final CompoundParameter speed =
    new CompoundParameter("Speed", .5)
    .setDescription("Speed of the effect");

  private final ModelBuffer modelBuffer = new ModelBuffer(lx);

  private float base = 0;

  public Sizzle(LX lx) {
    super(lx);
    addParameter("amount", this.amount);
    addParameter("speed", this.speed);
  }

  @Override
  public void run(double deltaMs, double amount) {
    int[] buffer = this.modelBuffer.getArray();

    double amt = amount * this.amount.getValue();

    if (amt > 0) {
      base += deltaMs * .01 * speed.getValuef();
      for (int i = 0; i < buffer.length; ++i) {
        int val = (int) LXUtils.minf(0xff, 500 * LXUtils.noise(i, base, 0));
        buffer[i] = 0xff000000 | val | (val << 8) | (val << 16);
      }
      for (LXPoint p : model.points) {
        colors[p.index] = LXColor.multiply(this.colors[p.index], buffer[p.index], amt);
      }
    }
  }
}