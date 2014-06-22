
	instances F1 :: (T -> T -> Bool)

	instances X :: (Int[3], [T])

	instances T :: _
	instances E :: _

	fn foo´ :: ({([T], E[])} e -> E) = x

	op cons [+] :: ([T] l -> E e -> [T])
	
	fn div [/] :: Int a -> Int b -> Int!
		= a
		
	unit Digit :: Char '0 .. '9
	
	dimension Bool :: = [ False, True ]

	dimension Bit :: = [ `0, `1 ]
	
	dimension Time [T] :: Natural
	
	unit Seconds [sec] :: Time

	val January :: Int = '1
	val Day :: Hours='24h
	
	protocol List :: = { cons, size }	
	
	fault div-by-zero! :: Int '0 .. '0
	
	notation JSON :: 
	
	unit Year [Y] :: Int <~> (Digit, Digit, Digit, Digit)
	
	data Point :: (X- x, Y- y) <~> (X-, Colon, Y-)
	data Points :: [Point]
	
	unit system SI :: =
		ratio Time :: [
			'1h   = '60min,
			'1min = '60sec,
			'1sec = '1000ms 
		]
		ratio XY :: [ 'x$ = 'y€	]


	fn max :: (Int a -> Int b -> Int) 
	  \ a > b \ = a 	
	  = b
		
		
	fn quicksort :: [T] list -> [T] 
		\ list length <= '1 \
			= list 
		= (less ++ same ++ more)
	where
            T pivot  = list head
            [T] less = list filter pivot >  | quicksort
            [T] same = list filter pivot ==
            [T] more = list filter pivot <  | quicksort
            
	fn something :: [T] list -> {(T, T)}
		= { a => b, 
		    c => d }

	instances K :: _
	instances V :: _
	fn assoc [=>] :: K key -> V value -> (K, V) = (key, value)

	fn switch :: (Weekday d -> String)
		\ d == :
		\ Monday \= "Monday"
		\ Tuesday \= "Tuesday" 
		
	fn a-native-fn :: (String s -> String) = :native
	
	fn another :: [Int] n -> Int idx -> Int
		\ n == :
		\ []   \= '0
		\ ['1] \= '1
		\      \= n at idx	
		
	fn clojure :: [T] e -> (T, T) 
		= ((a b),
		   (b c),
		   (c d))
	
	fn clojure :: [] e -> () = :native
	
	fn range :: Int low -> Int high -> [Int]
		= :native 
	
	dimension Suit :: = { Spades, Hearts, Diamonds, Clubs }
	
	dimension Month :: Int '1 .. '12 = [Januar, Februar, December]
	
	unit Int :: Number <~> (Sign?, Digits)
	unit Float :: Number <~> (Int, Dot, Digits)

	dimension Char ['] :: Number #x0000 .. #xFFFF
	unit Digit :: Char '0' .. '9'

	data Digits :: Digit..

	unit Sign :: Char {'+', '-'}
	
	dimension Time :: Int
	unit Days :: `relative Time 
	unit DayOfMonth :: `absolute Time
	
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
	
	protocol List :: = {force, cons, append, concat, take, 
	                                    drop, remove, insert, at, slice}
	                                    
	                                    
	instances E :: _

	data Elements :: (
	    Length length,
	    E[]* elements,
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

	fn lazy :: (A -> P -> B) f -> A v -> P p -> ^B
	    = () -> (a f p)
        
        
	val Hour :: Milliseconds = '1h
	val Xyz :: Seconds = '2h + '42min
	
	instances T :: _
	fn or-default :: T? v -> T default -> T 
	    \ v exists \= v
	    \          \= default	
	    
	instances T :: _ & eq
	fn first :: [T] list -> T sample -> Index start -> T
	    \ sample == list at start \= e
	    \                        \= list first sample (start + '1) 
	    
	dimension Coordinate :: Int
	dimension X- :: Coordinate
	dimension Y- :: Coordinate
	unit Colon :: Char {':'}
	data Point :: (X- x, Y- y) <~> (X-, Colon, Y-)
	data Points :: [Point]
	
	fn move :: Point p -> Int dx -> Int dy -> Point
		= (p `x + dx, p `y + dy)
	
	val Max :: Point = "2:3"
	
	data String :: [Char]
	data Octal :: Char[8]
	
	instances T :: _
	instances S :: T
	fn specialise [~>] :: T value -> $S type -> S 
		= (`ast `specialise ?value ?type) 
	
	val Bla :: String = """
	
something very long with "quotes" in it; also having empty double quotes "" 
or even source code like 

	fn foo :: (A a -> B) = a bar
  
	"""
	
	instances A :: _
	instances B :: _
	instances F :: (A -> P -> B)
	
	fn invoke :: F f -> A a -> P p -> B = a f p
	
	fn map :: [A] l -> (A -> B) fn -> [B]
	fn singleton :: A v -> {A} = {v}
	
	val Setify :: ([A] -> [{A}]) = (_ map singleton)
	
	val SomeTHING :: = `some-thing
	
	fn test :: Int i -> Int = .math
	
	
	
	notation JSON ::

	fn show :: JSON v -> String 
		= "?"
	
	data Array :: ( [JSON] elements )
	
	fn show :: Array a -> String
		= a elements show
	
	data Object :: ( [Member] members )
	data Member :: ( Name name, JSON value )
	
	fn show :: Object o -> String
		= o members show
	
	fn show :: Member m -> String
		= m name ++ "," ++ m value show
		
	instances T :: (Name, ..)
	notation Tag :: T 
	
	fn signum :: Relation v -> Int
		\ v is Less \= '-1
		\ v is More \= '1
		             = '0
		             
	fn real :: Int v -> Real
		= '100,000.00e-34
		
	data Array2D :: Int[2][2]..
	
	instances S :: :(,)
	
	fn plus [+] :: Int a -> Int b -> Int! = (`ast `iadd ?a ?b)
	
	fn partially-ast-impl :: Some a -> Thing 
		\ foo bar \= (`ast baz)
		= (`ast que)
	
		
	instances A :: 6-8
	
	data Tuple :: (Int[A] one, String[A] other)
	
	data Embedding :: :(EmbeddedFoo foo, EmbeddedBar bar)
	data Embedding :: :(Embedded a, @Referenced b)
	
	instances P :: @_
	
	instances M :: *
	instances L :: 0-M

	fn fill :: T[L] a -> T e -> T[L]
		= a fill-with-from-to '0 M
	
	instances E :: _
	instances A :: E[*]
	
	fn same-length-array :: A a -> A b -> A		
	
	fn rotate180 :: Matrix* m -> Matrix 
		= m rotate90 rotate90
		
	instances F1 :: (->)
	instances F2 :: (_ -> _)
	instances F3 :: (_ -> _ -> _)
	instances O1 :: _(->)
	
	fn empty? :: E[] array -> Bool = array length == '0
	
	val Pi :: Int = (:= pi-gauss-legendre '0.000001 )
	
	fn first :: = at '0
	
	fn example :: E.. one -> E.. other -> Bool
		= one ^first == other ^first
		
	val Menu :: Food[Weekday] = ["Pasta", "Pizza"]