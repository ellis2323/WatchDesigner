#version 120
#ifdef GL_ES
precision highp float;

uniform lowp sampler2D iChannel0;

const mediump float smoothing = 2.0/16.0;
const mediump float smoothing2 = 4.0/16.0;

varying mediump vec2 vTexCoord0;
varying lowp vec4 vColor;
varying float vVar;

#else

uniform sampler2D iChannel0;

const float smoothing = 2.0/16.0;
const float smoothing2 = 4.0/16.0;

varying vec2 vTexCoord0;
varying vec4 vColor;
varying float vVar;

#endif

void main()
{
    float distance = texture2D(iChannel0, vTexCoord0).a;
    float value = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    vec4 baseColor = vec4(vColor.rgb, value * vColor.a);
    float value2 = smoothstep(0.5 - smoothing2, 0.5, distance);
    float value3 = 1.0 - smoothstep(0.5, 0.5 + smoothing2, distance);
    vec4 glowColor = vec4(vColor.rgb, value2*value3*vColor.a*vVar);
    gl_FragColor = baseColor+glowColor;
}

