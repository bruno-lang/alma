namespace math ::

	use lang
	use strict util
	
	share strict vector
	share foo : x;
	share strict vector : a where
		where 
			Int = Number
			Float = Real
			eq = equal 