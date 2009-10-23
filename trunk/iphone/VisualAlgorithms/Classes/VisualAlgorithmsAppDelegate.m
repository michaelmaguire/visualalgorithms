//
//  VisualAlgorithmsAppDelegate.m
//  VisualAlgorithms
//
//  Created by Michael Maguire on 23/10/2009.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import "VisualAlgorithmsAppDelegate.h"
#import "EAGLView.h"

@implementation VisualAlgorithmsAppDelegate

@synthesize window;
@synthesize glView;

- (void) applicationDidFinishLaunching:(UIApplication *)application
{
	[glView startAnimation];
}

- (void) applicationWillResignActive:(UIApplication *)application
{
	[glView stopAnimation];
}

- (void) applicationDidBecomeActive:(UIApplication *)application
{
	[glView startAnimation];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
	[glView stopAnimation];
}

- (void) dealloc
{
	[window release];
	[glView release];
	
	[super dealloc];
}

@end
