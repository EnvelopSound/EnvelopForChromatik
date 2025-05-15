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

package us.envelop;

import heronarts.lx.utils.LXUtils;

public interface EnvelopConstants {
  public final static int NUM_COLUMNS = 8;
  public final static float INCHES = 1;
  public final static float FEET = 12*INCHES;

  // Compatibility with legacy P3 noise
  public default float noise(float x) {
    return .5f + .5f * LXUtils.noise(x, 0, 0);
  }

  // Compatibility with legacy P3 noise
  public default float noise(float x, float y) {
    return .5f + .5f * LXUtils.noise(x, y, 0);
  }

  // Compatibility with legacy P3 noise
  public default float noise(float x, float y, float z) {
    return .5f + .5f * LXUtils.noise(x, y, z);
  }
}
