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


	fn max :: (Int a -> Int b -> Int) 
	  \ a > b \ = a 	
	  = b
		
		
	fn quicksort :: [T] list -> [T] 
		\ list # < '1 \
			= list 
		= (less ++ equal ++ more)
	where
            T pivot   = list head
            [T] less  = list filter pivot >  | quicksort
            [T] equal = list filter pivot ==
            [T] more  = list filter pivot <  | quicksort
            
	fn something :: [T] list -> {(T, T)}
		= { a => b, 
		    c => d }

	fn switch :: (Weekday d -> String)
		\ d == ?
		\ :monday \= "Monday"
		\ :tuesday \= "Tuesday" 
		
	fn a-native-fn :: (String s -> String) = &native
	
	fn another :: [Int] n -> Int idx -> Int
		\ n == ?
		\ []   \= '0
		\ ['1] \= '1
		\      \= n at idx	
		
	fn clojure? :: [T] e -> (T, T) 
		= ((a b),
		   (b c),
		   (c d))
	
	fn clojure? :: [] e -> () = &native
	
	fn range :: () x -> [Int]
		= '0 .. '12 
	
	dimension Suit :: = { :spades, :hearts, :diamonds, :clubs }
	
	dimension Month :: Int '1 .. '12 = [:januar, :februar, :december]
	
	unit Int :: Number <-> (Sign?, Digits)
	unit Float :: Number <-> (Int, Dot, Digits)

	dimension Char ['] :: Number #x0000 .. #xFFFF
	unit Digit :: Char '0' .. '9'

	data Digits :: Digit..

	unit Sign :: Char {'+', '-'}
	
	dimension Time :: Int
	unit Days :: relative Time 
	unit DayOfMonth :: absolute Time
	
	instances E :: _
	instances L :: [E]
	
	op cons :: L l -> E e -> L
	op append :: L l -> E e -> L
	op concat :: L l -> L other -> L
	op take :: L l -> Count c -> L
	op drop :: L l -> Count c -> L
	op remove :: L l -> Index i -> L
	op insert :: L l -> Index i -> L
	op at :: L l -> Index i -> E?
	op slice :: L l -> Index from -> Index to -> L
	
	protocol List :: {force, cons, append, concat, take, 
	                                    drop, remove, insert, at, slice}
	                                    
	                                    
	instances E :: _

	data Elements :: (
	    Length length,
	    E[]~ elements,
	    [E] tail
	)
	
	fn at :: Elements list -> Index i -> E?
	    \ i < list length \= list at i
	    \                 \= list tail at (i - list length)
	
	fn insert :: Elements list -> Index i -> T e -> [E]
	    \ i == '0 \= list prepand e
	    \ i == '1 \= list take '1 append e ++ (list drop '1)
	    \ i >= list length \= (list length + '1, elements, tail insert at (i - list length))
	    = (list take i) ++ (drop i prepand e)
	    
	instances P :: (,)
	instances A :: _
	instances B :: _

	fn lazy :: (A -> P -> B) f -> A v -> P p -> (() -> B)
	    = () -> (a f p)
        
        
	val :1hour :: Milliseconds = '1h
	val :x :: Seconds = '2h + '42min
	
	instances T :: _
	fn or-default :: T? v -> T default -> T 
	    \ v exists \= v
	    \          \= default	
	    
	instances T :: _ & eq
	fn first :: [T] list -> T sample -> Index start -> T
	    \ sample == list @ start \= e
	    \                         \= list first sample (start + '1) 
	    
	dimension Coordinate :: Int
	dimension X- :: Coordinate
	dimension Y- :: Coordinate
	unit Colon :: Char {':'}
	data Point :: (X- x, Y- y) <-> (X-, Colon, Y-)
	data Points :: [Point]
	
	val :p1 :: Point = "2:3"
	
	data String :: [Char]
	data Octal :: Char[8]
	
