<?php

// This script updates a legacy LXP project file for EnvelopLX to run on
// the current version of Chromatik (1.1.0+ at time of writing).
//
// The projects expect the Envelop for Chromatik package to be installed,
// references to patterns and some modulations paths are updated.
//
// Known issues:
// - Some MIDI mapping paths will not load properly
  
error_reporting(E_ALL | E_NOTICE);

$content = file_get_contents('php://stdin');

$find = array(
  // Core stuff
  'heronarts.lx.pattern.GradientPattern' => 'heronarts.lx.pattern.color.GradientPattern',
  'heronarts.lx.pattern.IteratorPattern' => 'heronarts.lx.pattern.test.TestPattern',  
  'heronarts.p3lx.pattern.SolidPattern' => 'heronarts.lx.pattern.color.SolidPattern',
  'heronarts.lx.effect.DesaturationEffect' => 'us.envelop.effect.Desaturation',

  // Envelop patterns
  'EnvelopLX$Blips' => 'us.envelop.pattern.Blips',
  'EnvelopLX$Bouncing' => 'us.envelop.pattern.Bouncing',
  'EnvelopLX$Bugs' => 'us.envelop.pattern.Bugs',
  'EnvelopLX$ColumnNotes' => 'us.envelop.pattern.ColumnNotes',
  'EnvelopLX$EnvelopDecode' => 'us.envelop.pattern.Decode',
  'EnvelopLX$Flash' => 'us.envelop.pattern.Flash',
  'EnvelopLX$Helix' => 'us.envelop.pattern.Helix',
  'EnvelopLX$Jitters' => 'us.envelop.pattern.Jitters',
  'EnvelopLX$Noise' => 'us.envelop.pattern.Noise',
  'EnvelopLX$NotePattern' => 'us.envelop.pattern.NotePattern',
  'EnvelopLX$EnvelopObjects' => 'us.envelop.pattern.Objects',
  'EnvelopLX$Raindrops' => 'us.envelop.pattern.Raindrops',
  'EnvelopLX$Rings' => 'us.envelop.pattern.Rings',
  'EnvelopLX$EnvelopShimmer' => 'us.envelop.pattern.Shimmer',
  'EnvelopLX$Sparkle' => 'us.envelop.pattern.Sparkle',
  'EnvelopLX$Starlight' => 'us.envelop.pattern.Starlight',
  'EnvelopLX$Swarm' => 'us.envelop.pattern.Swarm',
  'EnvelopLX$Tron' => 'us.envelop.pattern.Tron',
  'EnvelopLX$Warble' => 'us.envelop.pattern.Warble',
  
  // Envelop effects
  'EnvelopLX$ArcsOff' => 'us.envelop.effect.ArcsOff',
  'EnvelopLX$LSD' => 'us.envelop.effect.LSD',
  'EnvelopLX$Sizzle' => 'us.envelop.effect.Sizzle',
  'EnvelopLX$Strobe' => 'us.envelop.effect.Strobe',
  
  // MIDI mapping parameter paths
  '"parameterPath": "spreadX"' => '"parameterPath": "xAmount"',
  '"parameterPath": "spreadY"' => '"parameterPath": "yAmount"',
  '"parameterPath": "spreadZ"' => '"parameterPath": "zAmount"',
  
  // us.envelop.pattern.Flash pattern parameter conflict rename
  '"midiFilter":' => '"midiNoteFilter":'
);

$content = str_replace(array_keys($find), array_values($find), $content);
$json = json_decode($content, true);

$json['version'] = '1.1.0';

function engine($key) {
  global $engine;
  $ret = $engine[$key];
  unset($engine[$key]);
  return $ret;
}

$engine = $json['engine'];

$mixer = array(
  'class' => 'heronarts.lx.mixer.LXMixerEngine',
  'channels' => engine('channels'),
  'parameters' => engine('parameters')
);

$json['model'] = array(
  'class' => 'heronarts.lx.structure.LXStructure',
  'file' => 'Envelop/EnvelopSF.lxm',
  'parameters' => array(
    'syncModelFile' => 'true'
  )
);

$palette = engine('palette');
$palette['children'] = array(
  'swatch' => array(
    'colors' => array(
      array(
        'id' => 9812734,
        'parameters' => array(
          'primary/brightness' => $palette['parameters']['color/brightness'],
          'primary/saturation' => $palette['parameters']['color/saturation'],
          'primary/hue' => $palette['parameters']['color/hue']
        )
      )
    )
  )
);

function envelop($key) {
  global $engine;
  $params = $engine['components']['envelop'][$key]['parameters'];
  return array(
    'parameters' => array(
      'gain' => $params['Gain'],
      'range' => $params['Range'],
      'attack' => $params['Attack'],
      'release' => $params['Release']
    )
  );
}

$json['engine'] = array(
  'id' => 1,
  'class' => 'heronarts.lx.LXEngine',
  'children' => array(
    'osc' => engine('osc'),
    'midi' => engine('midi'),
    'modulation' => engine('modulation'),
    'mixer' => $mixer,
    'palette' => $palette,
    'output' => engine('output'),
    'audio' => array(
      'children' => array(
        'envelop' => array(
          'children' => array(
            'source' => envelop('source'),
            'decode' => envelop('decode')
          ),
          'internal' => array(
            'metersExpanded' => true
          ),
          'parameters' => array(
            'running' => true
          )
        )
      )
    )
  )
);

$json['externals'] = array(
  'ui' => array(
    'leftPaneActiveSection' => 2,
    'audioExpanded' => false,
    'envelopExpanded' => true,
    'preview' => array(
      'pointCloud' => array(
        'ledStyle' => 3,
        'ledStyle/name' => "LENS3",
        'pointSize' => 3.0,
        'alphaRef' => 0,
        'feather' => 1.0,
        'sparkle' => 1.0,
        'sparkleCurve' => 2.0,
        'sparkleRotate' => 45.0
      ),
      
      'camera' => array(
        'active' => true,
        'radius' => 424.87518339155673,
        'theta' => 170.88596076425165,
        'phi' => 14.603137810016051,
        'x' => -1.0310628437437117,
        'y' => -10.039536856114864,
        'z' => 2.757044769823551
      ),
      'axes' => array(
        'visible' => false
      )
    )
  )
);

print(json_encode($json, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES));
echo "\n";
