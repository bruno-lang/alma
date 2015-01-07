
	family F1 :: (T -> T -> Bool)

	family X :: (Int[3], [T])

	family T :: _
	family E :: _

	fn foo :: ({([T], E[])} e -> E) = x

	op cons [+] :: ([T] l -> E e -> [T])
	
	fn div [/] :: Int a -> Int b -> Int!		
		= a
		
	unit Digit :: Char{'0'..'9'}

	dimension Float :: Number{-2e18..+2e18} %, NaN %
	
	dimension Bool :: = [ False, True ]
	
	dimension Bool :: () = [ False, True ]

	dimension Bit :: = [ `0 `1 ]
	
	dimension Time :: Natural
	
	unit Seconds :: Time

	val January :: Int = 1
	val Day :: Hours=24h
	
	behaviour List :: = { cons, size }	
	
	fault Div-by-zero! :: Int{0}
	
	notation JSON :: 
	
	unit Year :: Int : (Digit Digit Digit Digit)
	
	data Point :: (X- x, Y- y) : (X- ':' Y-)
	data Points :: [Point]
	
	dimension SI :: ()
	
	ratio Time :: SI = {
		1h   = 60min,
		1min = 60sec,
		1sec = 1000ms 
	}
	ratio XY :: SI = { 2$ = 1€ }


	fn max :: (Int a -> Int b -> Int) =
	  a > b : a 	
	        : b
		
		
	fn quicksort :: [T] list -> [T] =
		list length <= '1 : list 
		                  : (less ++ same ++ more)
	where
            T pivot  = list head
            [T] less = list filter pivot >  | quicksort
            [T] same = list filter pivot ==
            [T] more = list filter pivot <  | quicksort
            
	fn something :: [T] list -> {(T, T)}
		= { a => b, 
		    c => d }

	family K :: _
	family V :: _
	fn assoc [=>] :: K key -> V value -> (K, V) = (key, value)

	fn switch :: (Weekday d -> String) =
		Monday  : "Monday"
		Tuesday : "Tuesday"
	where
		?. d == _ :
		
	fn another :: [Int] n -> Int idx -> Int =
		a. []  : 0
		b. [1] : 1
		c.     : n at idx
	where
		?. n == _ :
		
	fn pairs :: Point p -> Int =
		1 , 2 : 1
		0 , 4 : 2
		      : 3
	where
		?. p x == _, p y == _ :
		
	fn clojure :: [T] e -> (T, T) 
		= ((a b),
		   (b c),
		   (c d))
	
	fn range :: Int low -> Int high -> [Int]
		= (`ast `range ?low ?high) 
	
	dimension Suit :: = { Spades, Hearts, Diamonds, Clubs }
	
	dimension Month :: Int{1..12} = [Januar, Februar, December]
	
	unit Int :: Number : (Sign?, Digits)
	unit Float :: Number : (Int '.' Digits)

	dimension Char :: Number{#x0000..#xFFFF}
	unit Digit :: Char{'0'..'9'}

	data Digits :: Digit[1-*]

	unit Sign :: Char{'+'|'-'}
	
	dimension Time :: Int
	unit Days :: `relative Time 
	unit DayOfMonth :: `absolute Time
	
	family E :: _
	family L :: [E]
	
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
	                                    
	                                    
	family E :: _

	data Elements :: (
	    Length length,
	    E[]* elements,
	    [E] tail
	)
	
	fn at :: Elements list -> Index i -> E? =
	    i < list length : list at i
	                    : list tail at (i - list length)
	
	fn insert :: Elements list -> Index i -> T e -> [E] =
	    i == 0 : list prepand e
	    i == 1 : list take 1 append e ++ (list drop 1)
	    i >= list length : (list length + '1, elements, tail insert at (i - list length))
	                     : (list take i) ++ (drop i prepand e)
	    
	family P :: (,)
	family A :: _
	family B :: _

	fn lazy :: (A -> P -> B) f -> A v -> P p -> ~B
	    = () -> (a f p)
        
        
	val Hour :: Milliseconds = 1h
	val Xyz :: Seconds = 2h + 42min
	
	family T :: _
	fn or-default :: T? v -> T default -> T =
	    v exists : v
	             : default	
	    
	family T :: _ with eq
	fn first :: [T] list -> T sample -> Index start -> T =
	    sample == list at start : e
	                            : list first sample (start + '1) 
	    
	dimension Coordinate :: Int :(Bit[32])
	dimension X-Coordinate :: Coordinate
	dimension Y-Coordinate :: Coordinate
	data Point :: (X-Coordinate x, Y-Coordinate y) 
				: (X-Coordinate ':' Y-Coordinate)
				: (X-Coordinate..Y-Coordinate)
	data Points :: [Point]
	
	fn move :: Point p -> Int dx -> Int dy -> Point
		= (p `x + dx, p `y + dy)
	
	val Min :: Point = "2:3"
	val Max :: Point = 2:3
	
	data String :: [Char]
	data Octal :: Char[8]
	
	family T :: _
	family S :: T
	fn specialise [+>] :: T value -> $S type -> S 
		= (`ast `specialise ?value ?type) 
	
	val Bla :: String = """
	
something very long with "quotes" in it; also having empty double quotes "" 
or even source code like 

	fn foo :: (A a -> B) = a bar
  
	"""
	
	family A :: _
	family B :: _
	family F :: (A -> P -> B)
	
	fn invoke :: F f -> A a -> P p -> B = a f p
	
	fn map :: [A] l -> (A -> B) fn -> [B] = "XXX"
	fn singleton :: A v -> {A} = {v}
	
	val Setify :: ([A] -> [{A}]) = (_ map singleton)
	
	val SomeTHING :: = `some-thing
	
	fn test :: Int i -> Int = Math x
	
	
	
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
		
	family T :: (Name, ..)
	notation Tag :: T 
	
	fn signum :: Relation v -> Int =
		v is Less : -1
		v is More : 1
		          : 0

	fn real :: Int v -> Real
		= 100,000.00e-34
		
	data Array2D :: Int[2][2]
	
	fn plus [+] :: Int a -> Int b -> Int! = (`ast `add ?a ?b)
	
	fn partially-ast-impl :: Some a -> Thing =
		foo bar : (`ast baz)
		        : (`ast que)
	
		
	family A :: 6-8
	
	data Tuple :: (Int[A] one, String[A] other)
	
	family M :: *
	family L :: 0-M

	fn fill :: T[L] a -> T e -> T[L]
		= a fill-with-from-to '0 M
	
	family E :: _
	family A :: E[*]
	
	fn same-length-array :: A a -> A b -> A	= "?"
	
	fn rotate180 :: Matrix* m -> Matrix 
		= m rotate90 rotate90
		
	family F1 :: (->)
	family F2 :: (_ -> _)
	family F3 :: (_ -> _ -> _)
	family O1 :: ?(->)
	
	fn empty? :: E[] array -> Bool = array length == 0
	
	val Pi :: Float = (:= pi-gauss-legendre 0.000001 )
	
	fn first :: = at 0
	
	fn call-side-inline :: E[1-*] one -> E[1-*] other -> Bool
		= one\last == other\last
		
	val Menu :: Food[Weekday] = ["Pasta", "Pizza"]
	
	val Menu :: Food[Weekday] = { Monday => "Pasta", Tuesday => "Pizza" }
	
	proc assoc [=>] :: K key -> V value -> (K, V) = (key, value)
	
	family E :: _
	fn at :: E[<>] s -> Index i -> Int = (`ast `get ?s ?i)
	
	% keys %
	
	family V :: _
	fn get :: T obj -> @V key -> V? = @akey-in-action-but-impl-doesn't-make-sense
	
	fn put :: T obj -> @V key -> V value -> T = ?
	
	fn canonical-name :: @V key -> String = ?
	
	fn on-channel :: Int[>] chan -> Int[>] = ?
		
	fn yields-channel :: @T[>] key -> T[>] = ?

	% == [ Processes & Channels ]== %

	% blocking, non blocking and unknown output %
	family O1 :: _[>]
	family O2 :: _]>[
	family O3 :: _]>]
	
	% blocking, non blocking and unknown input %
	family I1 :: _[<]
	family I2 :: _]<[
	family I3 :: _]<]
	
	% processes %
	process Server :: = { Ready => [ Ready ], _ => [ Ready ] }
	
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
		1. Work: worker responds >> (worker process (worker requests <<))
		2. Register: @pool send-queue >> (worker requests key)
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
		= res
		where
		Int res
			|= value >> channel
			|= value >> (timeout make-channel) than-return channel
		
		
	fn example :: T[>][3] channels -> T = value
	where 
		T value 
			|= channels at 0 <<
			|= channels at 1 <<
			|= channels at 2 <<	
	
	fn example2 :: Int[>] channel -> Int = res
	where
		Int res
			|= channel <<
			|= 7 after 6ms 
			
	fn example3 :: Int[>] channel -> Int = res
	where
		Int res
			|= channel <<
			|= 2
		
	% Streams %
	
	family O :: _>
	family I :: _<
	
	fn append :: Byte> file -> [Byte] bytes -> Byte> = file ++ b
	where
		Byte b =<< bytes 

	key @process-pool :: @Worker[>][<>]
	
	fn numbers! :: Foo f -> Bar
		= [1/2 1.2/4]
		
	fn force-plus [+!] :: Int! a -> Int b -> Int!
		= (`ast (`add ?a ?b))
		
	% Shapes %
	
	fn shapes :: ([] x -> {}) = "example"
	fn shapes :: "" x -> String = "another example"
	fn shapes :: `foo x -> String = "another example"
	fn shapes :: @foo x -> String = "another example"

	fn show :: True t -> String = "true"
	fn show :: False f -> String = "false"

	family A :: (`ast , ..)

	% testing some things... %
	
	data Menu :: Meal[Weekday]
	
	when Greet :: Char> out -> ()
		1. out print "Hello World"
		.. 	
		
	% Behaviours (again) %
	
	family X :: _ with eq, compare
	
	family S :: [E] as Stack E with at
	
	behaviour Stack E :: = { push, pop }
	family S2 :: _ as Stack E, Collection E
	
	
	family T :: (,)
	family F :: (->)
	family A :: _[]
	family V :: _[<>]
	family L :: [_]
	family S :: {_}
	family D :: *
	
	family O :: ?(->)
	family R :: _[<]
	family W :: _[>]
	
	family M :: _?
	family E :: _!
	family P :: _*
	
	family T :: $_
	family L :: ~_
	family K :: @_	
	
	family E :: _ as Stack, Collection
	family F :: _ as Stack with plus
	
	fn contains :: Char[*] arr -> Char sample -> Bool 
		= arr index-of (sample, '0) exists?
	where
		equals-ignore-case +> eq Char	
		
	data Point :: (Coordinate x, Coordinate y) 
	            : (Coordinate..Coordinate)
	            : ('(' Coordinate ':' Coordinate ')')
	            : (Coordinate[*], Coordinate[*])[*]
	            
	unit Minutes :: Time : (Minutes 'min')
	
	val Era :: Date = 1970-01-01
	
	family S1 :: _[<*>]
	family S2 :: _[<2>]
	family S3 :: _[<2-4>]