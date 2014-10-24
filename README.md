
<h1>Sarpaper Android app</h1>
Questa app è stata realizzata da Arpa Piemonte nell'ambito di un progetto finalizzato alla valutazione dell'esposizione personale al telefono cellulare. Il progetto è stato finanziato dal Comitato Regionale per le Comunicazioni (CoReCom) del Piemonte.

<h3>Descrizione</h3>
Il progetto è stato realizzato da Arpa Piemonte con lo scopo di conoscere i livelli di esposizione ai campi elettromagnetici a radiofrequenza emessi dai cellulari nelle diverse modalità di utilizzo. L'esposizione è stata misurata per mezzo di un sistema sperimentale, realizzato appositamente, in grado di rilevare la potenza trasmessa dal telefono cellulare durante chiamate vocali o traffico dati.. Gli esiti dell'indagine hanno fornito interessanti indicazioni sulla variabilità dell'esposizione personale in funzione dell'utilizzo di segnali 3G o 2G e dei livelli di ricezione ambientale del segnale.

Con questa app, realizzata all'interno dello stesso progetto, si è voluto costruire uno strumento di monitoraggio dell'uso del telefono cellulare al fine di progettare anche specifici studi su gruppi di utilizzatori.

I dati registrati dalla app sono relativi a parametri quali il tempo di utilizzo con dispositivi che riducono sensibilmente l'esposizione (viva voce, auricolari) ed il tempo di utilizzo in assenza di tali dispositivi ed in condizioni di esposizione suddivise in tre fasce, bassa-media-alta.

I dati sopra riportati, insieme ad altre informazioni di dettaglio sul tipo di segnale utilizzato nelle chiamate, possono essere inviati ad un server ftp per poter effettuare analisi di esposizione su un campione di utilizzatori.

La app consente di monitorare i seguenti parametri:
<ul>
<li>tempo di ogni chiamata vocale</li>
<li>tipo di rete su cui avviene il traffico</li>
<li>livello de segnale ricevuto in dBm</li>
<li>eventuale dispositivo utilizzato (viva voce o auricolare)</li>
</ul>
Per quanto riguarda il tipo di rete, sono identificabili quindici diversi protocolli di comunicazione:
<ul>
<li>tre relativi a tecnologia 2G(GPRS, EDGEeiDen)</li>
<li>undici relativi a tecnologia 3G(CDMA,UMTS,eHRPD,HSPA, HSPA+, HSDPA, HSUPA, EVDO rev 0-A-B, 1xRTT)</li>
<li>uno relativo a tecnologia 4G (LTE)</li>
</ul>

I dati rilevati dalla app vengono registrati in un file log che può essere inviato ad un server ftp e che viene denominato secondo la seguente codifica:
UUID + "_" + PhoneModel + "_" + DATAORA + ".log" dove:
<ul>
    <li>il codice identificativo UUID è l'IMEI del telefonino</li>
    <li>la data e ora sono nel seguente formato: AA + MM + GG + HH + mm + SS</li>
</ul>

Il tracciato interno del file log ha una prima riga contenente il modello e il codice identificativo del telefonino, mentre le altre righe contengono i dati oggetto del monitoraggio e, in particolare, saranno costituite da records strutturati nel seguente modo:
ORA + "," + SECS + "," + TIPO + "," + DBM + "," + DEVICE

Riportiamo di seguito una illustrazione dei dati che vengono riportati nei records:
<ul>
    <li>ORA - ora della chiamata nel formato HH + mm + SS</li>
    <li>SECS - somma del tempo trascorso raggruppando per : TIPO+DBM+DEVICE i secondi rilevati;</li>
    <li>TIPO - codice tipologia di rete utilizzata (esempio EDGE, UMTS ecc.) Le 15 tipologie considerate vengono identificate con un numero da 1 a 15. E' previsto anche il codice 0 in caso di tipologia di rete sconosciuta.</li>
    <li>DBM - livello di segnale ricevuto dal telefonino e rilevato in dBm. Questo valore sarà positivo e pari a 99 in caso di non rilevazione di segnale da parte del telefono.</li>
    <li>DEVICE - tipo di dispositivo audio in ascolto utilizzato. Questo campo assumerà i seguenti valori:
        <ul>
            <li>0, in assenza di utilizzo di dispositivi</li>
            <li>1 con viva voce inserito, con o senza auricolare</li>
            <li>2 con auricolare inserito e senza viva voce</li>
        </ul>
    </li>
</ul>







