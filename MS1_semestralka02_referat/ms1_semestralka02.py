# -*- coding: utf-8 -*-

#importy
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

"""## 1) Načtení dat signálu"""

#stazeni signalu z githubu
#! git clone https://github.com/Fiiila/MS1_2022.git

#nacteni signalu pomoci pandas
signal_pd = pd.read_csv("MS1_2022/signal.csv",header=None)
#prevod na numpy
signal = signal_pd.to_numpy()
signal = signal[:,0]

"""## 2) Zobrazení časového vývoje signálu"""

#vytvoreni casove osy na zaklade frekvence signalu
freq = 80000 #Hz
print(f"frequency of signal: {freq} Hz")
T = 1/freq
print(f"signal T: {T} s")
time = np.array(range(0,len(signal)))*T
plt.figure()
plt.plot(time, signal)
plt.xlabel("time [s]")
plt.ylabel("signal value")
plt.title("Průběh importovaného signálu v čase")

"""## 3) Určení časových parametrů signálu"""

#zobrazeni parametru signalu
print(f'stredni hodnota signalu: {signal.mean():.4f}')
energy = signal.dot(signal)*T
print(f'energie signalu: {energy:.4f}')
power = (1/(T*len(signal)))*energy
print(f'vykon signalu: {power:.4f}')
print(f'efektivni hodnota signalu: {np.sqrt(power):.4f}')

"""## 4) Určení frekvenčních parametrů signálu"""

# spektrum signálu
spectrum = np.fft.fft(signal)
power_spectrum = abs(spectrum)/len(signal)
frequencies = np.fft.fftfreq(len(spectrum),1/freq)
i = frequencies>0
plt.figure()
plt.plot(frequencies[i],2*power_spectrum[i])
plt.xlabel("frequency f [Hz]")
plt.ylabel("|X(f)|")
plt.savefig("power_spectrum.eps", format="eps")
# dominantní frekvence
print(f'dominantni frkvence: {frequencies[np.argmax(abs(spectrum))]}Hz')

"""## 5) Ověření principu neurčitosti"""

#okenkova funkce
plt.figure(figsize=(15,5))
# mensi okenko
plt.subplot(1,2,1)
plt.title("Spektrogram s okénkem $2^8$")
plt.xlabel("čas [s]")
plt.ylabel("frekvence [Hz]")
spectrogram1 = plt.specgram(signal,Fs=freq,NFFT=2**8,mode="default", window=plt.mlab.window_hanning)
# vetsi okenko
plt.subplot(1,2,2)
plt.title("Spektrogram s okénkem $2^{12}$")
plt.xlabel("čas [s]")
plt.ylabel("frekvence [Hz]")
spectrogram2 = plt.specgram(signal,Fs=freq,NFFT=2**12,mode="default", window=plt.mlab.window_hanning)

"""## 6) Nalezení časo-frekvenční události

Na vykreslených grafech lze pozorovat 4 časo-frekvenční události v časech 4s, 5s, 14s a 15s.
"""

#nalezeni caso-frekvencnich udalosti
spectrogram = spectrogram1
#suma jednotlivych sloupcu
column_sum = np.sum(spectrogram[0],axis=0)
#vykresleni sum v jednotlivych casovych okamzicich
plt.figure()
plt.scatter(np.arange(0,(len(time)/2**12),(len(time)/2**12)/len(column_sum))*(T*2**12),column_sum)
#vypsani maxim
print(f"casy ve kterych byla nalezena maxima: {np.argpartition(column_sum,-4)[-20:]*((len(time)/2**12)/len(column_sum))*(T*2**12)}")