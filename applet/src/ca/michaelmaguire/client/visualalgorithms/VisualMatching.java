// Visual_Matching.java - Michael Maguire - 14 Apr 96

package ca.michaelmaguire.client.visualalgorithms;

import java.applet.*;

import java.awt.*;

/**
 * Permission to open source this code from Luis Goddyn, Simon Fraser University.  Facebook message thread below:
 * 
 * Geocities going down -- moving VisualAlgorithms
 * Between Luis Goddyn and Michael Maguire
 * 
 *  Michael Maguire October 8, 2009 at 5:28pm
 *  
 *  Luis,
 * 
 * For years now I've had that project we did up on:
 * 
 * http://www.geocities.com/michael.maguire/VisualAlgorithms/
 * 
 * 
 * but apparently Yahoo! is shutting down Geocities soon.
 * 
 * I think it was a cool project and I'd hate to see it go away.
 * 
 * I was going to move that project and host it somewhere else, probably Google.
 * 
 * 
 * While I was in the process of doing that, I was thinking of checking in the source code on some free code hosting service like code.google.com. Usually, however, to check stuff in on one of these it must be Open Source.
 * 
 * Can I get your permission to Open Source the code for that?
 * 
 * Cheers,
 * Michael
 * 
 * Luis Goddyn October 8, 2009 at 7:10pm
 * 
 * It absolutely should be open source. Never did get it up to where I really wanted it. It was a great project! 
 * 
 */
public class VisualMatching extends Applet
{

	private GraphPanel	the_graph;

	private Scrollbar	delay_control;

	private Scrollbar	increment_control;

	public void init()
	{

		setLayout(new BorderLayout());

		the_graph = new GraphPanel();

		/** goddyn */
		// the_graph.resize( 1900,1900 );
		// System.out.println("the_graph size: " + the_graph.size().width + " "
		// + the_graph.size().height );

		add("Center", the_graph);

		Panel buttons = new Panel();

		buttons.add(new Button("Go"));

		buttons.add(new Button("Stop"));

		buttons.add(new Button("Single step"));

		buttons.add(new Button("Reset"));

		buttons.add(new Button("Delete"));

		buttons.add(new Button("Clear all"));

		/** goddyn */
		if (getParameter("PRINTPOINTSBUTTON") != null)
		{
			buttons.add(new Button("Print Points"));
		}

		add("South", buttons);

		Panel algorithms = new Panel();

		Choice algorithm = new Choice();

		algorithm.addItem("Non-Bipartite Perfect Matching");

		algorithm.addItem("Bipartite Perfect Matching");

		algorithm.addItem("Spanning Tree");

		algorithm.addItem("Bipartite Vertex Cover");

		algorithms.add(algorithm);

		CheckboxGroup bipartite = new CheckboxGroup();

		Checkbox blue = new Checkbox("Blue", bipartite, false);

		the_graph.bipartite_colour(blue);

		algorithms.add(blue);

		algorithms.add(new Checkbox("Red", bipartite, true));

		add("North", algorithms);

		/** goddyn */
		Panel controls = new Panel();
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		controls.setLayout(gridBag);

		Label title_label = new Label("Animation", Label.LEFT);
		title_label.setFont(new Font("Helvetica", Font.BOLD, 12));
		c.gridwidth = 2;
		c.weighty = 0.1f;
		gridBag.setConstraints(title_label, c);
		controls.add(title_label);

		delay_control = new Scrollbar(Scrollbar.VERTICAL, 11, 3, 0, 50);
		c.gridwidth = 1;
		c.weighty = 0.9f;
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 1;
		gridBag.setConstraints(delay_control, c);
		controls.add(delay_control);

		increment_control = new Scrollbar(Scrollbar.VERTICAL, 2, 5, 0, 60);
		c.gridx = 1;
		gridBag.setConstraints(increment_control, c);
		controls.add(increment_control);

		Label delay_label = new Label("Delay", Label.CENTER);
		c.weighty = 0.0f;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 2;

		Label increment_label = new Label("Increment", Label.CENTER);
		gridBag.setConstraints(delay_label, c);
		controls.add(delay_label);

		c.gridx = 1;

		gridBag.setConstraints(increment_label, c);
		controls.add(increment_label);

		// the_graph.animation_delay = delay_control.getValue();
		the_graph.animation_delay = (int) (5 * Math.exp((double) delay_control.getValue() / 10.0));
		// the_graph.growth_increment = (float) increment_control.getValue();
		the_graph.growth_increment = (float) Math.exp((double) increment_control.getValue() / 10.0);

		add("East", controls);

		/** endgoddyn */

		// Input Inital points

		String pointListString = getParameter("INITIALPOINTS");
		// System.out.println( "pointListString: " + pointListString);

		if (pointListString != null)
		{
			int[] pointsListArray = new int[pointListString.length()];
			int firstDigitPos = 0;
			int lastDigitPos = 0;
			int intCounter = 0;

			for (boolean MoreNumbersToParse = true; MoreNumbersToParse;)
			{
				try
				// Find first digit in string
				{
					for (firstDigitPos = lastDigitPos; "0123456789".indexOf(pointListString.charAt(firstDigitPos)) == -1; firstDigitPos++)
					{
						// Do nothing -- just looking for first digit.
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					MoreNumbersToParse = false;
				}

				try
				// Find last digit in string
				{

					for (lastDigitPos = firstDigitPos + 1; lastDigitPos < pointListString.length()
							&& "0123456789".indexOf(pointListString.charAt(lastDigitPos)) != -1; lastDigitPos++)
					{
						// Do nothing -- just locating last consecutive digit.
					}

				}
				catch (StringIndexOutOfBoundsException e)
				{
					// MoreNumbersToParse = false;
				}

				try
				// Convert substring into integer
				{
					// System.out.println("Digit Positions: " + firstDigitPos +
					// " " + lastDigitPos);
					// System.out.println("Number String: " + (
					// pointListString.substring(firstDigitPos,lastDigitPos) )
					// );

					pointsListArray[intCounter++] = (new Integer(pointListString.substring(firstDigitPos, lastDigitPos)))
							.intValue();

					// System.out.println("count: " + intCounter +
					// "    Number : " + pointsListArray[ intCounter-1] );
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}

			}

			for (int pointCounter = 2; pointCounter < 3 * (intCounter / 3); pointCounter += 3)
			{
				// System.out.println("Adding a node");
				the_graph.add_node(pointsListArray[pointCounter - 2], pointsListArray[pointCounter - 1],
						pointsListArray[pointCounter] != 0 // if == 0 then don't
						// change colour
						// from default
						);
			}

		}
		/** endgoddyn */

		repaint();

	}

	public void start()
	{
		the_graph.start();
	}

	public void stop()
	{
		the_graph.stop();
	}

	public String getAppletInfo()
	{
		return "Visual Matching by Michael A. Maguire, May 1997";
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public boolean action(Event evt, Object arg)
	{
		if (evt.target instanceof Choice)
		{

			String algorithm_string = (String) arg;

			the_graph.change_algorithm(algorithm_string);

		}

		if ("Go".equals(arg))
		{

			showStatus("Loading algorithm, please wait...");

			the_graph.go();

			showStatus("Algorithm in progress");

			return true;

		}

		if ("Stop".equals(arg))
		{
			the_graph.please_stop();

			showStatus("Algorithm stopped");

			return true;

		}

		if ("Single step".equals(arg))
		{
			showStatus("Loading algorithm, please wait...");

			the_graph.step();

			showStatus("Algorithm in progress");

			return true;

		}

		if ("Reset".equals(arg))
		{

			the_graph.reset_to_nodes_only();

			return true;

		}

		if ("Delete".equals(arg))
		{

			the_graph.delete();

			return true;

		}

		if ("Clear all".equals(arg))
		{

			the_graph.reset_everything();

			return true;

		}

		if ("Print Points".equals(arg))
		{

			the_graph.print_points();

			return true;

		}

		return false;

	}

	/** goddyn */
	public boolean handleEvent(Event evt)
	{
		switch (evt.id)
		{
		case Event.SCROLL_LINE_UP:
		case Event.SCROLL_LINE_DOWN:
		case Event.SCROLL_PAGE_UP:
		case Event.SCROLL_PAGE_DOWN:
		case Event.SCROLL_ABSOLUTE:
			if (evt.target == delay_control)
			{
				// the_graph.animation_delay = ((Integer)evt.arg).intValue();
				// the_graph.animation_delay = delay_control.getValue();
				the_graph.animation_delay = (int) (5 * Math.exp((double) delay_control.getValue() / 10.0));
			}

			if (evt.target == increment_control)
			{
				the_graph.growth_increment = (float) Math.exp((double) ((Integer) evt.arg).intValue() / 10.0);
			}
		}
		return super.handleEvent(evt);
	}
	/** endgoddyn */

}
