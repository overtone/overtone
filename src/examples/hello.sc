( // press CTRL+E here
{
  RLPF.ar(
    Saw.ar(55),
    LFNoise1.kr([5, 5], mul: 440, add: 880),
    0.1,
    mul: 0.25
  )
}.play;
)
