module math ::
	
	-auto eq =<> equals Int
	-auto [Digit] =<> Digits
	-auto [] =<> NoElements
	
	-invariant Digits ! non-empty
	-invariant Int ! positive
	
	instances F1 :: (T -> T -> Bool)

	instances X :: (Int[3], [T])

	instances T :: _
	instances E :: _

	fn foo* :: ({([T], E[])} e -> E) = x

	op cons [+] :: ([T] l -> E e -> [T])
	
	fn div [/] :: Int a -> Int b -> Int!
		= a
		
	fn mod :: (Int a -> Int b -> Int)
		= b
		
	unit Digit :: Char '0 .. '9

	dimension Bit :: = [ :0, :1 ]
	
	dimension Time [T] :: Natural
	
	unit Seconds [sec] :: Time

	val :january :: Int '1
	val :day :: Hours '24h
	
	protocol List :: { cons, size }	
	
	fault div-by-zero! :: Int '0 .. '0
	
	notation JSON :: 
	
	data Object :: ( 
		[Member] members,
		Foo bar 
	) <-> Byte[] <-> (X, Y) = [ :foo, :bar ]
	
	unit system SI :: =
		ratio Time :: [
			'1h   = '60min,
			'1min = '60sec,
			'1sec = '1000ms 
		]
		ratio XY :: [ 'x$ = 'y%	]
