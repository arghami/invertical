invertical
==========

******************************
*           COS'E'           *
******************************

InvertiCal è un plugin che vi permette di sapere quanti punti avreste fatto se la sorte vi avesse assegnato un calendario di un'altra squadra. Quante volte avete invidiato qualcuno perchè gli capita sempre l'avversario che fa meno gol? Ecco, ora potrete avere i dati reali a vostra disposizione per aumentare il vostro rammarico...



******************************
*        INSTALLAZIONE       *
******************************

Il programma è scritto in java, per cui requisito indispensabile per farlo funzionare è la presenza di una java virtual machine, che potete scaricare dal seguente indirizzo:
http://www.java.com/it/download/manual.jsp
oppure sul sito della sun.
Può essere che sul vostro pc sia già installata una versione di java. In tal caso, non dovrete scaricare nulla.

Per utilizzare il programma è sufficiente scompattare il pacchetto in un punto qualsiasi del vostro pc. Se lo volete utilizzare come plugin per FCM, dovete mettere i file "invertical.bat" e "invertiCal.jar" nella cartella plugin di FCM e seguire le istruzioni della sezione "Utilizzo".



******************************
*          UTILIZZO          *
******************************

Il programma funziona solo su leghe gestite con Fantacalcio Manager. Può funzionare in due modalità:
- Stand Alone
- Plugin per il sito di FCM.


   Modalità Stand Alone
Per utilizzare questa modalità è sufficiente fare doppio click sul file "invertiCal.jar" (in questo caso il .bat non serve a niente). Il programma vi chiederà di specificare il file .fcm della vostra lega da cui prelevare i dati. Dopodichè vi verrà chiesto di specificare la competizione su cui effettuare il calcolo. L'output verrà generato nella stessa cartella in cui avete lanciato il programma.
NB: se il .jar è associato a un'altra applicazione (ad esempio Nokia PC Suite) dovrete eseguirlo cliccando col destro e poi selezionando "apri con... -> java platform" (o qualcosa del genere). Se non ci riuscite nemmeno così, aprite una finestra di DOS, portatevi nella cartella dove c'è il jar e digitate:
java -jar invertiCal.jar


   Modalità Plugin
Mettete i file "invertical.bat" e "invertiCal.jar" nella cartella plugin di FCM; aprite il file invertical.bat col blocco note e controllate che il percorso
cd c:\programmi\FCM\plugin
corrisponda effettivamente alla cartella in cui avete installato FCM. In caso contrario, correggetela manualmente.
Inserite nel .ini della vostra skin il riferimento al file invertical.bat: potete metterlo sia tra i "prima" che tra i "dopo", l'esecuzione non cambia.
Il plugin verrà quindi lanciato all'atto della generazione del sito, e vi verrà richiesto di selezionare la competizione e quindi il girone nell'ambito della competizione. L'output viene salvato all'interno del sito generato.



******************************
*         LIMITAZIONI        *
******************************

Per la sua natura, il programma può funzionare solo su competizioni con un "reale" calendario e con UN NUMERO PARI DI SQUADRE. Per intenderci, se provate a invertire il calendario di una coppa o di una competizione a gironi, non ho idea di cosa vi possa saltare fuori...
PER ORA il programma non tiene conto dei modificatori di modulo. Tiene invece conto (a meno di bug) di tutte le altre regole impostate nella lega (modificatori classici, fasce di punteggio e regole relative, fattore campo...)



******************************
*           OUTPUT          *
******************************

L'output generato è composto da:

- cartella "incroDet"
  +
  |____ ricalcolo di ogni singola giornata, con tutti i possibili incroci, in formato html
  |____ ricalcolo di ogni singola giornata, con tutti i possibili incroci, in formato txt
  |____ somma dei punti ottenuti lungo tutte le giornate, con tutti i possibili incroci, in formato html
  |____ somma dei punti ottenuti lungo tutte le giornate, con tutti i possibili incroci, in formato txt

- cartella "js"
  +
  |____ somma dei punti ottenuti lungo tutte le giornate, con tutti i possibili incroci, in formato javascript

- log.txt: contiene uno storico dell'esecuzione, utile (per me) per capire se ci sono stati degli errori.

Gli output generati dal plugin sono estremamente grezzi. Per avere una grafica accattivante, scaricate anche i file di grafica sviluppati da Maelstrom (cercate sul forum per sapere l'indirizzo esatto).