# QA-Server
Question Answering - Programma Server

Sistema di question answering inerente al mondo del cinema

Il progetto consiste nello sviluppo di un piccolo sistema di Question Answering (QA).

Il sistema proposto consiste nei seguenti moduli: sistema di indicizzazione della base di conoscenza (Indexer), sistema di interrogazione (Searcher) e l’interfaccia in linguaggio naturale, ovvero il sistema di QA vero e proprio (Machine).
Il programma è offerto in versione client / server e la sua interfaccia grafica è stata implementata mediante la libreria JavaFX. Il programma server, attivo 24h, è in esecuzione su un Virtual Private Server Aruba.

La base di conoscenza

Per poter rispondere alle domande che gli verranno poste, il sistema ha bisogno di immagazzinare quante più informazioni possibili relativamente allo scibile umano. A tal fine, si è scelto di utilizzare l’archivio cinematografico di una grande base di conoscenza collaborativa, Freebase (attualmente confluita in Google). Gli elementi fondamentali nella base di conoscenza sono i topic (ovvero concetti e individui), i tipi dei topic e le proprietà dei tipi. Ad esempio, Homer Simpson ha il seguente id univoco: m.0h545 (sotto forma di URL si ottiene una pagina concatenando alla URL base http://www.freebase.com/ l’ID del topic sostituendo slash al punto, cioè: http://www.freebase.com/m/0h545 rappresenta Homer Simpson in HTTP)

Download e formato dei dati in input

La versione di Freebase ridotta al dominio del cinema è scaricabile da:

http://babelware.org/pensieroprofondo/fb_triples_film.gz

Il file è strutturato come un elenco di relazioni (soggetto, predicato, oggetto) in formato N3 ovvero una tripla per ogni linea. Il file contiene solo topic collegati ai film, per un totale di 127 milioni di triple (17G scompattato e 1.7G compattato). Il numero di predicati unici è pari a 13258 ed il numero di topic è 4555082. 
Per la lettura del file delle triple di Freebase viene letto direttamente il file .gz, senza scompattarlo, utilizzando la classe GZIPInputStream:

    FileInputStream fin = new FileInputStream(FILENAME);
    GZIPInputStream gzis = new GZIPInputStream(fin);
    InputStreamReader isr = new InputStreamReader(gzis);
    BufferedReader br = new BufferedReader(isr);

Esempi di predicati che coinvolgono i topic come soggetti

Ad esempio, un topic è m.0h545, che rappresenta Homer Simpson. Ecco alcune triple che lo riguardano:

<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/2000/01/rdf-schema#label>    "Homer Simpson"@et      .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/2000/01/rdf-schema#label>    "Homer Simpson"@pt      .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/2000/01/rdf-schema#label>    "Homer Simpson"@cs      .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/2000/01/rdf-schema#label>    "Homer Simpson"@da      .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/2000/01/rdf-schema#label>    "Houmeris Simpsonas"@lt .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/2000/01/rdf-schema#label>    "호머 심슨"@ko  .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/common.topic>       .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/tv.tv_character>    .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/fictional_universe.fictional_character>     .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/award.award_nominee>        .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/theater.theater_character>  .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/base.thesimpsons.topic>     .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/base.breakfast.breakfast_cereal_mascot>   .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/base.italiantv.adapted_tv_character>        .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/film.film_character>        .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/base.breakfast.topic>       .
<http://rdf.freebase.com/ns/m.0h545>    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>       <http://rdf.freebase.com/ns/base.ontologies.ontology_instance>  .

La prima riga si traduce in label(m.0h545, “Homer Simpson”@et), ovvero che m.0h545 si dice in Estone “Homer Simpson”. L’ottava riga, invece, specifica type(m.0h545, tv.tv_character), ovvero che Homer è di tipo tv.tv_character, così come, nelle righe seguenti, che è di tipo fictional_universe.fictional_character, film.film_character ecc. 

Esempi di predicati che coinvolgono i tipi come soggetti

I tipi sono normalmente specificati da stringhe separate da punto, ad esempio tv.tv_character oppure base.thesimpsons.topic, theater.theater_character. Anche i tipi possono avere un tipo a loro volta, ad es.:

<http://rdf.freebase.com/ns/award.award_winner> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>   	<http://www.w3.org/2000/01/rdf-schema#Class>


Moduli del progetto

Il progetto Server consta di 4 parti, ciascuna implementata in un package separato: 

Indicizzatore
Searcher
Question Answering
Server
it.uniroma1.lcl.pensieroprofondo.qa

Il package it.uniroma1.lcl , invece, dovrà contenere le classi Type, Topic (e, se implementate, le classi Language e Graph).


Indicizzatore

Questo modulo legge il file delle triple di Freebase e crea uno o più indici atti a contenere le informazioni della base di conoscenza, in particolare i topic, i tipi dei topic e le proprietà dei tipi.

Al fine dell’indicizzazione è stata utilizzata la libreria Apache Lucene. Il punto di accesso di questo modulo è la classe FreebaseIndexer che possiede il seguente costruttore:

public FreebaseIndexer(String nomeFileGz, String cartellaDestinazione)

e il metodo:

public void index()

che procede all’indicizzazione, ovvero alla creazione nella cartella di destinazione specificata della struttura dati su disco da accedere in seguito. 
La classe implementa anche la possibilità di restringere l’indicizzazione a un elenco di identificativi di topic e/o un elenco di identificativi tipi specificati.

Nel progetto è inclusa la classe Indexer, che rappresenta il punto di accesso dell’indicizzatore in quanto istanzia la classe FreebaseIndexer specificando la cartella di destinazione dell’indice da creare.

Searcher

Questo modulo legge l’indice o gli indici creati su disco per rispondere a singole interrogazioni. La classe Searcher è il punto di accesso. Di tale classe può esistere una sola istanza. La classe espone i seguenti metodi:

getTopic che, dato in input un id, restituisce un oggetto della classe Topic che permette di accedere a tutte le informazioni del concetto o individuo specificato. Ad esempio:

	Searcher s = Searcher.getInstance();
	Topic t = s.getTopic(“m.0h545”);

	restituisce il topic relativo a Homer Simpson.

getTopics che, dato in input un tipo, restituisce una collezione iterabile di Topic. Ad esempio:

	Type ty = new Type(“film.film_character”);
	for (Topic t : s.getTopics(ty))
	      for (String label : t.getLabels())
		System.out.println(t.getId()+” [”+label+”]: “+t.getDescription());
1
stampa l’elenco dei personaggi di film nel formato “id [nome]: descrizione”, uno per ogni riga. Sia i tipi che i topic possono essere specificati senza il prefisso dell’URL (ad es. http://www.w3.org/1999/02/ e http://rdf.freebase.com/ns/ possono essere omessi, risparmiando così molto spazio nell’indice). 

Topic.getLabels restituisce una collezione di etichette associata al topic (ovvero, le parole utilizzate per esprimere il topic) per la lingua di default del vostro progetto (inglese). Il metodo Topic.getLabels(Language) restituisce le etichette del topic per la lingua specificata.

Il metodo getTypes, dato un topic, restituisce una collezione dei tipi associati a quel Topic.
Il metodo getQuery, data in input un’interrogazione, restituisce una collezione di topic e/o tipi che rispondono all’interrogazione. Le interrogazioni sono oggetti che esprimono i seguenti tipi di vincoli (anche in congiunzione e/o disgiunzione):
L’oggetto in questione è un tipo, un topic o qualsiasi dei due
L’oggetto deve possedere una certa proprietà valorizzata (ad es. un topic deve avere tipo film.film_character oppure deve avere come attrice Nicole Kidman) o meno (ad es. l’oggetto deve avere un attore).

La classe Searcher legge automaticamente i file di indice generati dall'Indexer.  

La classe Language è dotata di un costruttore Language(String) che, presa in input una stringa rappresentate una lingua (cioè una stringa come "@en", "@it", "@ko", ecc.), restituisce un oggetto Language relativo a quella lingua.

Question Answering

Il terzo modulo è l’interfaccia di Question Answering verso l’utente. Esso rende disponibile una classe Machine che espone metodi per due modalità di interazione:

il metodo runConsole entra in un ciclo infinito: richiede un input da tastiera, ovvero la domanda da sottoporre al sistema, la elabora, restituisce la risposta all’utente e torna alla richiesta successiva.
il metodo query che, data in input una singola domanda sotto forma di stringa, restituisce la o le risposte a tale domanda.
Quest’ultimo metodo fornisce la risposta da inviare al client mediante l’interfaccia grafica.

Il sistema permette i seguenti tipi di domande in lingua inglese:

Che cosa è X? (ad es. “what is Matrix?”, “what is Lucasfilm?”)
Chi è X? (ad es. “who is Homer Simpson?”)
Dove è nato X?
Se il topic richiesto non è rispettivamente un’entità non vivente (“what is X?”) o una persona (“who is X?”, “where was X born?”), PensieroProfondo risponde adeguatamente.
“Who has directed X?”) dove “directed” è un’azione associata a un determinata proprietà (“film.film.directed_by”).
“Tell me X Y”, dove X è un numero e Y è un tipo (ad es. “tell me 10 cities”, dove cities è associato al tipo “location.citytown”; oppure, “Tell me 10 movies”, dove movies è associato al tipo “film.film”.
“Tell me Y of X”, dove X è un topic e Y è una stringa mappata a un predicato (ad esempio, “Tell me the genre of Pink Floyd  – The Wall”, dove “genre” è mappato al predicato “film.film_genre” e “Pink Floyd  – The Wall” è un topic) 

L’impossibilità di rispondere è gestita attraverso eccezioni appropriate; ad esempio, se Y non è un predicato, Machine risponde di non conoscere quell’informazione. Le risposte sono fornite dal metodo query come elenco dei topic o tipi che rispondono alla domanda, mentre il metodo runConsole restituisce le etichette (.getLabels()) di ciascun risultato (topic o tipo) all’utente.

Esempi supportati dalla quick scan (Si consiglia la quick scan in quanto il database completo rende estremamente lenta la ricerca della risposta all’interno dell’indice)

Seguono esempi di triple contenute nell’indice ridotto che rispondono alle domande specificate:

Q: tell me the genre of Pink Floyd  – The Wall

La seguente tripla contiene una possibile risposta (m.04t36, che corrisponde a Musical, è un genere di m.058ymm che corrisponde a Pink Floyd – The Wall):

<http://rdf.freebase.com/ns/m.04t36> <http://rdf.freebase.com/ns/film.film_genre.films_in_this_genre>        <http://rdf.freebase.com/ns/m.058ymm>   .

Q: who has directed Pink Floyd – The Wall?

La seguente tripla contiene una possibile risposta (m.1ycck è Alan Parker, il regista):

<http://rdf.freebase.com/ns/m.058ymm>   <http://rdf.freebase.com/ns/film.film.directed_by>  	 <http://rdf.freebase.com/ns/m.01ycck>   .

Q: who has written Pink Floyd – The Wall?

La seguente tripla contiene una possibile risposta (m.017g21 è Roger Waters):

<http://rdf.freebase.com/ns/m.058ymm>   <http://rdf.freebase.com/ns/film.film.written_by>   	 <http://rdf.freebase.com/ns/m.017g21>   .

Q: tell me the actors of Pink Floyd – The Wall

La seguente tripla contiene una possibile risposta. In questo caso la risposta non è specificata direttamente nella tripla, perché il topic oggetto del predicato (m.0y58gz5) rappresenta una collezione di proprietà, ovvero nello specifico una film performance:

<http://rdf.freebase.com/ns/m.058ymm>   <http://rdf.freebase.com/ns/film.film.starring> <http://rdf.freebase.com/ns/m.0y58gz5>

La collezione di proprietà in m.0y58gz5 è accessibile mediante altri predicati che mettono in relazione la film performance m.0y58gz5 con diversi topic. Ad esempio:

<http://rdf.freebase.com/ns/m.0y58gz5>   <http://rdf.freebase.com/ns/film/performance/actor> <http://rdf.freebase.com/ns/m.0gczs7>
<http://rdf.freebase.com/ns/m.0y58gz5>	  <http://rdf.freebase.com/ns/film/performance/film>	<http://rdf.freebase.com/ns/m.058ymm>
<http://rdf.freebase.com/ns/m.0y58gz5>	 <http://rdf.freebase.com/ns/film/performance/character>	<http://rdf.freebase.com/ns/m.0y58gz8>
<http://rdf.freebase.com/ns/m.0y58gz5>	 <http://rdf.freebase.com/ns/type/object/type>	<http://rdf.freebase.com/ns/film/performance>

I topic in questione sono: l’attrice Jenny Wright (m.0gczs7), il film Pink Floyd – The Wall (m.058ymm), il personaggio American Groupie (m.0y58gz8) e il tipo della collezione (film/performance).

Q: who is Jenny Wright?

PensieroProfondo sa che e' una persona:

<http://rdf.freebase.com/ns/people.person>      <http://rdf.freebase.com/ns/type.type.instance> <http://rdf.freebase.com/ns/m.0gczs7>   .

e che è una attrice:

<http://rdf.freebase.com/ns/film.actor> <http://rdf.freebase.com/ns/type.type.instance> <http://rdf.freebase.com/ns/m.0gczs7>   .

Q: where was Jenny Wright born?

PensieroProfondo sa che è nata a New York:

<http://r df.freebase.com/ns/m.02_286>   <http://rdf.freebase.com/ns/location.location.people_born_here> <http://rdf.freebase.com/ns/m.0gczs7>   .

Server

Il modulo Server è attivo 24h e in ascolto sulla porta TCP 8080. Spiegazioni dettagliate sono fornite dai commenti del codice sorgente.
