package matching;

import java.awt.*; 


class Natural_Number_Colour extends Object
{
		
	public static Color convert( short natural_number_colour )
	{
		Color c = new Color(255-(natural_number_colour-1)*57,
				(natural_number_colour-1)*(255-(natural_number_colour-2))*13,
				(255-(natural_number_colour-1))*41 );
		return(c);
	}
}
