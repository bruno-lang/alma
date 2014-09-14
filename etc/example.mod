
	instances F1 :: (T -> T -> Bool)

	instances X :: (Int[3], [T])

	instances T :: _
	instances E :: _

	fn foo :: ({([T], E[])} e -> E) = x

	op cons [+] :: ([T] l -> E e -> [T])
	
	fn div [/] :: Int a -> Int b -> Int!
		= a
		
	unit Digit :: Char '0 .. '9
	
	dimension Bool :: = [ False, True ]

	dimension Bit :: = [ `0 `1 ]
	
	dimension Time [T] :: Natural
	
	unit Seconds [sec] :: Time

	val January :: Int = '1
	val Day :: Hours='24h
	
	behaviour List :: = { cons, size }	
	
	fault Div-by-zero! :: Int '0 .. '0
	
	notation JSON :: 
	
	unit Year [Y] :: Int : "(Digit Digit Digit Digit)"
	
	data Point :: (X- x, Y- y) : "(X- ':' Y-)"
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
		\ list length <= '1 \= list 
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
		\ Monday \= "Monday"
		\ Tuesday \= "Tuesday"
	where
		\ d == _ \ 
		
	fn another :: [Int] n -> Int idx -> Int
		\ []   \= '0
		\ ['1] \= '1
		\      \= n at idx
	where
		\ n == _  \
		
	fn pairs :: Point p -> Int
		\ '1 , '2 \= '1
		\ '0 , '4 \= '2
		\         \= '3
	where
		\ p x == _, p y == _ \
		
	fn clojure :: [T] e -> (T, T) 
		= ((a b),
		   (b c),
		   (c d))
	
	fn range :: Int low -> Int high -> [Int]
		= (`ast `range ?low ?high) 
	
	dimension Suit :: = { Spades, Hearts, Diamonds, Clubs }
	
	dimension Month :: Int '1 .. '12 = [Januar, Februar, December]
	
	unit Int :: Number : "(Sign?, Digits)"
	unit Float :: Number : "(Int '.' Digits)"

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
	
	behaviour List :: = {force, cons, append, concat, take, 
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

	fn lazy :: (A -> P -> B) f -> A v -> P p -> ~B
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
	data Point :: (X- x, Y- y) : "(X- ':' Y-)"
	data Points :: [Point]
	
	fn move :: Point p -> Int dx -> Int dy -> Point
		= (p `x + dx, p `y + dy)
	
	val Max :: Point = "2:3"
	
	data String :: [Char]
	data Octal :: Char[8]
	
	instances T :: _
	instances S :: T
	fn specialise [=>>] :: T value -> $S type -> S 
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
		= a elements show?
	
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
	
	fn plus [+] :: Int a -> Int b -> Int! = (`ast `add ?a ?b)
	
	fn partially-ast-impl :: Some a -> Thing 
		\ foo bar \= (`ast baz)
		= (`ast que)
	
		
	instances A :: 6-8
	
	data Tuple :: (Int[A] one, String[A] other)
	
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
	
	val Pi :: Float = (:= pi-gauss-legendre '0.000001\precision )
	
	fn first :: = at '0
	
	fn call-side-inline :: E.. one -> E.. other -> Bool
		= one~last == other~last
		
	val Menu :: Food[Weekday] = ["Pasta", "Pizza"]
	
	val Menu :: Food[Weekday] = { Monday => "Pasta", Tuesday => "Pizza" }
	
	proc assoc [=>] :: K key -> V value -> (K, V) = (key, value)
	
	instances E :: _
	fn at :: E[:] s -> Index i -> Int = (`ast `get ?s ?i)
	
	% keys %
	
	instances V :: _
	fn get :: T obj -> @V key -> V? = @akey-in-action-but-impl-doesn't-make-sense
	
	fn put :: T obj -> @V key -> V value -> T
	
	fn canonical-name :: @V key -> String
	
	fn on-channel :: Int[>] chan -> Int[>]
	
	fn yields-channel :: @T[>] key -> T[>]

	% blocking, non blocking and unknown output %
	instances O1 :: _[>]
	instances O2 :: _]>[
	instances O3 :: _]>]
	
	% blocking, non blocking and unknown input %
	instances I1 :: _[<]
	instances I2 :: _]<[
	instances I3 :: _]<]
	
	% processes %
	process Server :: { Ready => [ Ready ], _ => [ Ready ] }
	
	% single process %
	when Ready :: HttpServer server -> HttpServer
		1. server responds >> (server process (server requests <<))
		.. Ready: server	

	% single process with where %		
	when Ready :: HttpServer server -> HttpServer
		1. server responds >> response
		.. Ready: server
	where
		Response response = server process request
		Request request = server requests <<
		
	% parallel with ad-hoc helper processes %
	when Ready :: HttpServer server -> HttpServer
		1. server respond! (server requests <<)
		.. Ready: server 

	fn respond! :: HttpServer server -> Request request -> ()
		=< server responds send! (server process request)	
		
	% parallel with process "pool" %
	when Ready :: HttpServer server -> HttpServer
		1. idle-worker-input >> (server requests <<)
		.. Ready: server
	where 
		HttpRequest[>] idle-worker-input = @pool receive-queue <<	

	when Idle :: Worker worker -> Worker
		1. worker responds >> (worker process (worker requests <<))
		2. @pool send-queue >> (worker requests key)
		.. Idle: worker
		
	when _! :: Worker worker -> Worker
		.. Init: worker 	
		
	when Out-Of-Memory! :: Worker worker -> Worker?
		..		
		
	% working with channels %
	
	fn broadcast :: T value -> [T]>]] channels -> ()
		= foreach (channel >> value)
	where
		T]>] channel = each channel	
		
	proc send? [>|>] :: T value -> T[>] channel -> Milliseconds timeout -> T[>]
		|= value >> channel
		|= value >> (timeout make-channel) than-return channel
		
		
	fn example :: T[>][3] channels -> T = value
	where 
		T value 
			|= channels at #0 <<
			|= channels at #1 <<
			|= channels at #2 <<	
	
	fn example2 :: Int[>] channel -> Int
		|= channel <<
		|= '7 after '6ms
			
	fn example3 :: Int[>] channel -> Int
		|= channel <<
		L= '2
		
	% Streams %
	
	instances O :: _>
	instances I :: _<
	
	fn append :: Byte> file -> [Byte] bytes -> Byte> = file ++ b
	where
		Byte b =<< bytes 

	key @process-pool :: @Worker[>][<>]
