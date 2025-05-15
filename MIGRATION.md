# Migration Guide from EnvelopLX

Ableton sets prepared for the legacy [EnvelopLX](https://github.com/envelopsound/EnvelopLX) software package will need minor updates for compability with [Chromatik](https://chromatik.co/).

## Enable the legacy OSC plugin

After installing the Envelop for Chromatik content package, ensure that you have enabled the `E4L Legacy OSC Support` plugin in Chromatik. This can be found in the application's left pane under the `CONTENT` tab.

<img width="186" alt="Screenshot 2025-05-15 at 3 48 01 PM" src="https://github.com/user-attachments/assets/5be68838-9759-4b15-a63b-1170a7346a71" />

This only needs to be done once. Restart Chromatik and the setting will persist any time Chromatik is run on the machine.

## OSC Changes

There are minor changes to some of the OSC paths used in Chromatik. These will need to be updated in instances of the [E4L LED Parameter Control](https://github.com/EnvelopSound/EnvelopForLive/wiki/E4L-LED-Parameter-Control) device.

#### Mixer channels

Mixer channels formerly referenced by `/lx/channel/N` need to be updated to refer to `/lx/mixer/channel/N`.

<img width="215" alt="Screenshot 2025-05-15 at 3 41 20 PM" src="https://github.com/user-attachments/assets/2909def5-d8b1-4537-b114-fabd8f1e3d4a" />

This example becomes: `/lx/mixer/channel/2/pattern/1/size`

#### Gradient pattern spread

Legacy gradient pattern paths `spreadX`, `spreadY` and `spreadZ` must be replaced by `xAmount`, `yAmount` and `zAmount`.

<img width="211" alt="Screenshot 2025-05-15 at 3 44 23 PM" src="https://github.com/user-attachments/assets/e0bc33ef-c10d-41fc-8c3b-1030fe40faf3" />

The first example becomes: `/lx/mixer/channel/6/pattern/1/xAmount`
  
