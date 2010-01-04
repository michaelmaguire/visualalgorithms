package matching;

import java.awt.*;

import java.lang.Integer;
import java.util.Vector;
import java.lang.Math;

class Node extends Object
{

	static final int	close_enough	= 10;

	short				node_number;

	float				x, y;

	Vector				disks			= new Vector(15);

	boolean				should_grow		= false;

	boolean				should_shrink	= false;

	boolean				blue			= false;

	boolean				selected;

	Node(int x1, int y1, boolean isblue, short node_number)
	{

		this.node_number = node_number;

		x = x1;

		y = y1;

		blue = isblue;

		if (blue)

		{

			disks.addElement((Object) new Disk(this, 0, Color.blue));

		}

		else

		{

			disks.addElement((Object) new Disk(this, 0, Color.red));

		}

	}

	void reset()
	{

		disks = new Vector(15);

		boolean should_grow = false;

		boolean should_shrink = false;

		if (blue)

		{

			disks.addElement((Object) new Disk(this, 0, Color.blue));

		}

		else

		{

			disks.addElement((Object) new Disk(this, 0, Color.red));

		}

	}

	boolean close_to(int x, int y)
	{

		if (Math.abs(this.x - x) < close_enough && Math.abs(this.y - y) < close_enough)
		{
			return (true);
		}
		else
		{
			return (false);
		}

	}

	void add_radius()
	{

		disks.addElement((Object) new Disk(this, ((Disk) disks.lastElement()).radius));
	}

	boolean remove_radius()

	{

		if (disks.isEmpty())
		{
			return (false);
		}
		else
		{
			// repair the order of the disk drawing before removing this disk
			((Disk) disks.lastElement()).prepare_to_remove();

			disks.removeElementAt(disks.size() - 1);

			return (true);
		}
	}

	void prepare_to_remove()
	{

		// keep removing all the radius until there are no more
		while (remove_radius())
			;

	}

	Disk outermost_disk()
	{

		return ((Disk) disks.lastElement());

	}

	void save(int x1, int y1)
	{

		x = x1;

		y = y1;

	}

	void paint(Graphics g)
	{

		g.setColor(Color.black);

		g.fillOval(((int) x) - 3, ((int) y) - 3, 6, 6);

		if (selected)
		{
			g.drawOval(((int) x) - 5, ((int) y) - 5, 10, 10);
		}

		if (blue)

		{

			g.setColor(Color.blue);

		}

		else

		{

			g.setColor(Color.red);

		}

		g.fillOval(((int) x) - 2, ((int) y) - 2, 4, 4);

		g.setColor(Color.black);

		g.drawString(Integer.toString(node_number), (int) x + 4, (int) y + 8);

	}

	float x()
	{

		return (x);

	}

	float y()
	{

		return (y);

	}

	float distance_to(Node other_node)
	{

		return ((float) (Math.sqrt((x - other_node.x) * (x - other_node.x) +

		(y - other_node.y) * (y - other_node.y))));

	}

	boolean is_blue()
	{

		return (blue);

	}

}
