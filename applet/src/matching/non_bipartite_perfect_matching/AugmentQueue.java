package matching.non_bipartite_perfect_matching;

import matching.AlgorithmSupport;

import java.util.Vector;

// May 1997 java version of:
// augment_queue_for_nb_p_m_pr.h -- Michael Maguire -- August 93

// used by nonbip_perfect_matching to "cheat" and make it appear to augment one step
// 	at a time, despite the fact that its recursive augmentation procedure requires
// 	that it actually do all the augmentation at once -- it will put all its augmentations
// 	into a queue implemented here and then empty the queue one augment step at a time
// 	as it is requested to do so by the algorithm manager

// each instance of the algorithm in nonbip_perfect_matching.c should keep its own 
// 	augment_queue -- however it shouldn't mess with the data structure directly but
// 	should instead call the appropriate function supplied by augment_queue_for_nb_p_m_pr.h
// 	to deal with these structures

class AugmentQueue extends Vector
{
	private static class AugmentQueueItem extends Object
	{
		private short	node_number1, node_number2;
		private boolean	marry;

		public AugmentQueueItem(short node_number1, short node_number2, boolean marry)
		{
			this.node_number1 = node_number1;
			this.node_number2 = node_number2;
			this.marry = marry;
		}

		public void output(AlgorithmSupport support)
		{

			if (marry)
			{

				support.output_remove_tight_line(node_number1, node_number2);
				support.output_marry(node_number1, node_number2);
			}
			else
			{
				support.output_divorce(node_number1, node_number2);
				support.output_add_tight_line(node_number1, node_number2);
			}
		}

	}

	void put_new_augment_item_into_queue(short node_number1, short node_number2, boolean marry)
	{

		addElement(new AugmentQueueItem(node_number1, node_number2, marry));
	}

	boolean is_there_an_item_to_do()
	{
		return (!isEmpty());
	}

	void do_next_augment_item(AlgorithmSupport support)
	{

		// output the first element
		((AugmentQueueItem) firstElement()).output(support);

		// remove the first element
		removeElement(firstElement());

	}

}
