#version 120
#ifdef GL_ES
#ifdef GL_OES_standard_derivatives
    #extension GL_OES_standard_derivatives : enable
#endif 

precision highp float;
uniform lowp sampler2D iChannel0;

varying mediump vec2 vTexCoord0;
varying lowp vec4 vColor;

const mediump float smoothing = 4.0/16.0;

#ifdef GL_OES_standard_derivatives

float contour(float d, float w) {
    // smoothstep(lower edge0, upper edge1, x)
    return smoothstep(0.5 - w, 0.5 + w, d);
}

float samp(vec2 uv, float w) {
    return contour(texture2D(iChannel0, uv).a, w);
}

#endif

void main()
{

#ifdef GL_OES_standard_derivatives

	// distance 0.5: edge <0.5: out >.5:in
	vec2 uv = vTexCoord0.xy;
    float dist = (texture2D(iChannel0, vTexCoord0).a);

    // fwidth helps keep outlines a constant width irrespective of scaling
    float width = 0.1 * fwidth(dist);
    float alpha = contour(dist, width);

    // Supersample, 4 extra points
    float dscale = 0.354; // half of 1/sqrt2; you can play with this
    vec2 duv = dscale * (dFdx(uv) + dFdy(uv));
    vec4 box = vec4(uv-duv, uv+duv);

    float asum = samp( box.xy, width ) + samp( box.zw, width ) + samp( box.xw, width ) + samp( box.zy, width );
    alpha = (alpha + 0.5 * asum) / 3.0;

    gl_FragColor = vec4(vColor.rgb, alpha);

#else 

    float distance = texture2D(iChannel0, vTexCoord0).a;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance) * vColor.a;
    gl_FragColor = vec4(vColor.rgb, alpha);

#endif
}

#else

// OPENGL VERSION

uniform sampler2D iChannel0;

varying vec2 vTexCoord0;
varying vec4 vColor;

const float smoothing = 4.0/16.0;

float contour(in float d, in float w) {
    return smoothstep(0.5 - w, 0.5 + w, d);
}

float samp(in vec2 uv, in float w) {
    return contour(texture2D(iChannel0, uv).a, w);
}

void main()
{
	// distance 0.5: edge <0.5: out >.5:in
	vec2 uv = vTexCoord0.xy;
    float dist = (texture2D(iChannel0, vTexCoord0).a);

    // fwidth helps keep outlines a constant width irrespective of scaling
    float width = 0.1 * fwidth(dist);
    float alpha = contour( dist, width );

    // Supersample, 4 extra points
    float dscale = 0.354; // half of 1/sqrt2; you can play with this
    vec2 duv = dscale * (dFdx(uv) + dFdy(uv));
    vec4 box = vec4(uv-duv, uv+duv);

    float asum = samp( box.xy, width ) + samp( box.zw, width ) + samp( box.xw, width ) + samp( box.zy, width );
    alpha = (alpha + 0.5 * asum) / 3.0;

    gl_FragColor = vec4(vColor.rgb, alpha);
}

#endif
