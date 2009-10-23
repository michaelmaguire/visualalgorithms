//
//  Shader.fsh
//  VisualAlgorithms
//
//  Created by Michael Maguire on 23/10/2009.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

varying lowp vec4 colorVarying;

void main()
{
	gl_FragColor = colorVarying;
}
