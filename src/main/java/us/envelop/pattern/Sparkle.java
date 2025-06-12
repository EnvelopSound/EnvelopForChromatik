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
import heronarts.lx.LXComponentName;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.utils.LXUtils;

@LXCategory("Envelop/Texture")
@LXComponentName("Legacy Sparkle")
public class Sparkle extends EnvelopPattern {

  public final SinLFO[] sparkles = new SinLFO[60];
  private final int[] map = new int[model.size];

  public Sparkle(LX lx) {
    super(lx);
    for (int i = 0; i < this.sparkles.length; ++i) {
      this.sparkles[i] = startModulator(new SinLFO(0, LXUtils.random(50, 120), LXUtils.random(2000, 7000)));
    }
    for (int i = 0; i < model.size; ++i) {
      this.map[i] = LXUtils.randomi(0, sparkles.length - 1);
    }
  }

  @Override
  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.gray(LXUtils.constrainf(this.sparkles[this.map[p.index]].getValuef(), 0, 100));
    }
  }
}
