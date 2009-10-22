// Visual_Algorithms.java - Michael Maguire - 14 Apr 96

package matching;


import java.applet.*;    
import java.awt.*; 


public class Visual_Matching extends Applet
{

	Graph_Panel the_graph;

	public void init()
	{
		setLayout(new BorderLayout());

		the_graph = new Graph_Panel();
		add("Center", the_graph);
		Panel p = new Panel();
		add("South", p);

		Choice algorithm = new Choice();
		algorithm.addItem("Bipartite Perfect Matching");
		algorithm.addItem("Spanning Tree");
		p.add(algorithm);

		p.add(new Button("Go"));
		p.add(new Button("Reset"));

		
		CheckboxGroup bipartite = new CheckboxGroup();
		Checkbox blue = new Checkbox("Blue", bipartite, false);
		the_graph.bipartite_colour( blue );
		p.add( blue );
		p.add( new Checkbox("Red", bipartite, true) );

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
			the_graph.step();
			return true;
		}

		if ("Reset".equals(arg))
		{
			the_graph.reset();
			return true;
		}
		return false;
	}
}

