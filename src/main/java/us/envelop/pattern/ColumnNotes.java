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
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.AHDSREnvelope;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;


@LXCategory("Envelop/MIDI")
public class ColumnNotes extends EnvelopPattern {

  private final ColumnLayer[] columns = new ColumnLayer[NUM_COLUMNS];

  public ColumnNotes(LX lx) {
    super(lx);
    int columnIndex = 0;
    for (LXModel column : getColumns()) {
      addLayer(columns[columnIndex] = new ColumnLayer(lx, column, columnIndex));
      addParameter("attack-" + columnIndex, columns[columnIndex].attack);
      addParameter("decay-" + columnIndex, columns[columnIndex].decay);
      columnIndex++;
    }
  }

  @Override
  public void noteOnReceived(MidiNoteOn note) {
    int channel = note.getChannel();
    if (channel < this.columns.length) {
      this.columns[channel].envelope.engage.setValue(true);
    }
  }

  @Override
  public void noteOffReceived(MidiNote note) {
    int channel = note.getChannel();
    if (channel < this.columns.length) {
      this.columns[channel].envelope.engage.setValue(false);
    }
  }

  private class ColumnLayer extends LXLayer {

    private final CompoundParameter attack;
    private final CompoundParameter decay;
    private final AHDSREnvelope envelope;

    private final LXModel column;

    private final LXModulator vibrato = startModulator(new SinLFO(.8, 1, 400));

    public ColumnLayer(LX lx, LXModel column, int columnIndex) {
      super(lx);
      this.column = column;

      this.attack = new CompoundParameter("Atk-" + columnIndex, 50, 25, 2000)
      .setExponent(4)
      .setUnits(LXParameter.Units.MILLISECONDS)
      .setDescription("Sets the attack time of the flash");

      this.decay = new CompoundParameter("Dcy-" + columnIndex, 1000, 50, 2000)
      .setExponent(4)
      .setUnits(LXParameter.Units.MILLISECONDS)
      .setDescription("Sets the decay time of the flash");

      final FixedParameter zero = new FixedParameter(0);
      this.envelope = new AHDSREnvelope("Env", zero, attack, zero, decay, zero, zero, zero, new FixedParameter(100));
      this.envelope.stageMode.setValue(AHDSREnvelope.StageMode.AD);

      addModulator(this.envelope);
    }

    @Override
    public void run(double deltaMs) {
      float level = this.vibrato.getValuef() * this.envelope.getValuef();
      for (LXPoint p : column.points) {
        colors[p.index] = LXColor.gray(level);
      }
    }
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }
}
