module math ::

	instances T :: _
	instances E :: _

	op cons [+] :: ([T] l -> E e -> [T])
	
	fn div [/] :: Int a -> Int b -> Int!
		= a
		
	fn mod :: (Int a -> Int b -> Int)
		= b
		
	unit Digit :: Char '0 .. '9

	dimension Bit :: = [ :0, :1 ]
	
	dimension Time [T] :: Natural
	
	unit Seconds [sec] :: Time

	val :january :: Int = '1
	
	protocol List :: { cons, size }	
	
	fault div-by-zero! :: Int '0 .. '0
	
	notation JSON :: 
	
	data Object :: ( 
		[Member] members,
		Foo bar 
	) <-> Byte[] <-> (X, Y) = [ :foo, :bar ]