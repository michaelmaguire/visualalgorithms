// Visual_Matching.java - Michael Maguire - 14 Apr 96

package matching;

import java.applet.*;    

import java.awt.*; 





public class Visual_Matching extends Applet

{



	private Graph_Panel the_graph;


	public void init()

	{

		setLayout(new BorderLayout());



		the_graph = new Graph_Panel();

		add("Center", the_graph);



		Panel buttons = new Panel();
		

		buttons.add(new Button("Go"));
		
		buttons.add(new Button("Stop"));

		buttons.add(new Button("Single step"));

		buttons.add(new Button("Reset"));

		buttons.add(new Button("Delete"));

		buttons.add(new Button("Clear all"));

		add("South", buttons );



		Panel algorithms = new Panel();


		Choice algorithm = new Choice();
		
		algorithm.addItem("Non-Bipartite Perfect Matching");
		
		algorithm.addItem("Bipartite Perfect Matching");

		algorithm.addItem("Spanning Tree");

		algorithm.addItem("Bipartite Vertex Cover");

		algorithms.add(algorithm);

		

		CheckboxGroup bipartite = new CheckboxGroup();

		Checkbox blue = new Checkbox("Blue", bipartite, false);

		the_graph.bipartite_colour( blue );

		algorithms.add( blue );

		algorithms.add( new Checkbox("Red", bipartite, true) );


		add("North", algorithms);



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

	public void update (Graphics g)

	{

		paint(g);



	}



	public boolean action(Event evt, Object arg)

	{



		if( evt.target instanceof Choice )

		{

			String algorithm_string = (String) arg;

			the_graph.change_algorithm( algorithm_string );

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

		return false;

	}

}
