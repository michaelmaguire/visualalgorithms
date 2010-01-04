package matching;

import java.awt.*;

class disk extends Object
{

	static disk	first_disk_to_be_drawn	= null;

	node		my_node;

	float		radius;

	Color		colour;

	disk		next_disk_to_be_drawn;

	/**
	 * algorithms which need moats must have the disks surrounding each node
	 * drawn in a particular order, otherwise the moats won't look right. this
	 * method uses a class variable to draw all the disks in the correct order
	 */

	static void paint_non_bipartite_disks_in_correct_order(Graphics g)
	{
		disk temp_disk;

		for (temp_disk = first_disk_to_be_drawn; temp_disk != null; temp_disk = temp_disk.next_disk_to_be_drawn)
		{
			temp_disk.paint(g);
		}
	}

	static void paint_bipartite_disks_in_correct_order(Graphics g)
	{
		disk temp_disk;

		// paint positive disks
		for (temp_disk = first_disk_to_be_drawn; temp_disk != null; temp_disk = temp_disk.next_disk_to_be_drawn)
		{

			if (temp_disk.radius >= 0)
			{
				temp_disk.bipartite_positive_paint(g);
			}
		}

		// paint negative disks
		for (temp_disk = first_disk_to_be_drawn; temp_disk != null; temp_disk = temp_disk.next_disk_to_be_drawn)
		{

			if (temp_disk.radius < 0)
			{
				temp_disk.bipartite_negative_paint(g);
			}
		}

		// paint all outlines of disks
		for (temp_disk = first_disk_to_be_drawn; temp_disk != null; temp_disk = temp_disk.next_disk_to_be_drawn)
		{
			temp_disk.bipartite_outline_paint(g);
		}

	}

	/**
	 * forces garbage collection of the static variable next_disk_to_be_drawn,
	 * which otherwise will continue to point to the disk objects, keeping them
	 * from being automatically garbage collected
	 */

	static void reset_disks()
	{
		first_disk_to_be_drawn = null;
	}

	/**
	 * repairs linked list of disks which are in correct order to be drawn so
	 * that when this disk is removed, the linked list will be intact without it
	 */
	void prepare_to_remove()
	{
		disk previous_disk, temp_disk;

		// special case -- first disk in list
		if (first_disk_to_be_drawn == this)
		{
			first_disk_to_be_drawn = first_disk_to_be_drawn.next_disk_to_be_drawn;
		}
		else
		{
			for (previous_disk = first_disk_to_be_drawn, temp_disk = first_disk_to_be_drawn.next_disk_to_be_drawn; temp_disk != null; previous_disk = temp_disk, temp_disk = temp_disk.next_disk_to_be_drawn)
			{
				if (temp_disk == this)
				{
					// we've found it
					previous_disk.next_disk_to_be_drawn = temp_disk.next_disk_to_be_drawn;
					return;
				}
			}
		}

		// if we made it to here, then the disk wasn't in the linked list which
		// is actually
		// an error condition, but we're not too worried about it

	}

	disk(node my_node, float radius)
	{

		this.my_node = my_node;

		this.radius = radius;

		this.colour = Color.red;

		this.next_disk_to_be_drawn = first_disk_to_be_drawn;

		first_disk_to_be_drawn = this;

	}

	disk(node my_node, float radius, Color colour)
	{

		this.my_node = my_node;

		this.radius = radius;

		this.colour = colour;

		this.next_disk_to_be_drawn = first_disk_to_be_drawn;

		first_disk_to_be_drawn = this;

	}

	void change_colour(Color the_colour)
	{

		colour = the_colour;

	}

	void paint(Graphics g)
	{

		g.setColor(colour);

		g.fillOval(Math.round(my_node.x - radius), Math.round(my_node.y - radius), Math.round(radius * 2), Math
				.round(radius * 2));

	}

	void bipartite_positive_paint(Graphics g)
	{

		g.setColor(colour);

		// g.fillOval((int)(my_node.x - radius)+1, (int)(my_node.y - radius)+1,
		// (int)(radius*2)-2, (int)(radius*2)-2 );
		g.fillOval(Math.round(my_node.x - radius), Math.round(my_node.y - radius), Math.round(radius * 2), Math
				.round(radius * 2));

	}

	void bipartite_outline_paint(Graphics g)
	{
		g.setColor(Color.black);

		if (radius >= 0)
		{
			g.drawOval(Math.round(my_node.x - radius), Math.round(my_node.y - radius), Math.round(radius * 2), Math
					.round(radius * 2));
		}
		else
		{
			g.drawOval(Math.round(my_node.x + radius), Math.round(my_node.y + radius), Math.round(-radius * 2), Math
					.round(-radius * 2));
		}

	}

	void bipartite_negative_paint(Graphics g)
	{

		if (colour.equals(Color.red))
		{
			g.setColor(Color.orange);
		}
		else
		{
			g.setColor(Color.green);
		}

		// g.fillOval((int)(my_node.x + radius)+1, (int)(my_node.y + radius)+1,
		// (int)(-radius*2)-2, (int)(-radius*2)-2 );
		g.fillOval(Math.round(my_node.x + radius), Math.round(my_node.y + radius), Math.round(-radius * 2), Math
				.round(-radius * 2));

	}

	void augment_radius(float amount_to_augment_by)
	{

		radius += amount_to_augment_by;

	}

	float radius()
	{

		return (radius);

	}

}
