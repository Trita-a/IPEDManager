# IPEDManager v1.2.1

## 🐛 Correzioni Critiche & Miglioramenti (v1.2.1)
- **Correzione Critica Allocazione RAM JVM (`-Xmx`)**:
  - Risolto il posizionamento del parametro `-Xmx` nel comando generato da `IpedExecutor`. Il parametro di memoria viene ora passato **prima** di `-jar iped.jar` come opzione standard della JVM (`java.exe -Xmx48G -jar ...`).
  - Questo garantisce che la Java Virtual Machine allochi realmente tutti i GB di memoria configurati dall'utente (es. 48 GB) invece di ignorare il parametro e ricadere sulla RAM minima di default.
- **Nuovo Riepilogo Risorse Pre-Avvio**:
  - Introdotto un popup informativo che precede l'avvio di ogni elaborazione, indicando la **RAM assegnata ad IPED** rispetto alla RAM totale del PC, il profilo forense e la cartella di output.
  - Include un'azione rapida per aprire direttamente le Impostazioni e modificare la memoria al volo prima di procedere.
- **Risoluzione Contrasto Titoli nei Dialoghi FlatLaf**:
  - Risolto il contrasto dei titoli su tutte le finestre secondarie, popup e dialoghi chiari, eliminando la scritta bianca su sfondo chiaro e garantendo testo scuro nitido ad alta leggibilità.
- **Miglioramento Spaziatura Tabella Evidenze**:
  - Aumentata l'altezza delle righe della tabella a 28px con margini laterali calibrati per dare respiro visivo alle icone, ai nomi dei file e al pulsante di eliminazione.
- **Nuova Icona Vettoriale RAM**:
  - Aggiunto il rendering vettoriale del banco di memoria RAM nel componente `VectorIcons`.

---

# IPEDManager v1.2.0

## 🚀 Novità Principali (v1.2.0)
- **Nuovo Cruscotto Prestazioni Hardware in Tempo Reale**:
  - Monitoraggio istantaneo e storico dei picchi per **CPU Sistema**, **RAM Globale**, **Disco Output** e **Cartella Temp / RAM Disk**.
  - **Metriche Dedicate al Processo IPED**: calcolo isolato in tempo reale della **CPU consumata da IPED** e della **RAM fisica reale** allocata dal processo, con indicazione del tetto massimo heap (`-Xmx`).
  - **Tracciamento Completo dell'Albero dei Processi**: scansione ricorsiva di tutti i sotto-processi worker generati da IPED (`ProcessHandle.descendants()`).
- **Console Log in Stile Modern IDE Terminal**:
  - Toolbar scura dedicata con badge conteggio righe in tempo reale.
  - Azioni rapide per **Copia Log**, **Pulisci** e nuova funzione **Salva su File (.txt)**.
- **Riprogettazione e Pulizia UI di ExecutionMonitorDialog**:
  - Header immersivo FlatLaf fuso con la barra del titolo e la `X` nativa della finestra.
  - Sottile ed elegante progress bar inferiore con stato descrittivo.
  - **Tasto Rapido "📁 Apri Cartella Output"**: compare automaticamente al termine dell'analisi per aprire direttamente il report del caso in Esplora Risorse.
  - Controllo accurato del codice di uscita (`exitCode`) del processo per segnalare fedelmente eventuali errori o interruzioni.

---

# IPEDManager v1.1.0

## Novità Principali (v1.1.0)
- **Integrazione Funzionalità di Intelligenza Artificiale (AI)**: Aggiunto supporto e interfaccia grafica per la configurazione dei moduli avanzati di IPED basati su AI, tra cui:
  - Rilevamento automatico materiale CSAM (Child Sexual Abuse Material).
  - Classificatore Remoto tramite API (Remote Image Classifier).
  - Stima dell'età dai volti (Age Estimation).
- **Integrazione RAM Disk**: Aggiunto supporto nativo per gestire dischi temporanei su RAM (OSFMount e Arsenal) per velocizzare le elaborazioni intensive (es. OCR).
- **Pulsante Gestione Profili**: Aggiunto un comodo pulsante di accesso rapido per la gestione profili accanto al menu a tendina.

## Bug Fix & Miglioramenti Architetturali
- **Isolamento dei Profili Forensi**: Riscritto il motore ConfigManager. Le impostazioni modificate vengono scritte *esclusivamente* nel profilo attivo, risolvendo il grave bug che causava la sovrascrittura distruttiva della configurazione base.
- UX migliorata: eliminati i noiosi popup di avviso al cambio di profilo.
- Risolti problemi visivi (doppie scrollbar e icone mancanti) nella UI.
