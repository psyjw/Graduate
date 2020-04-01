%tensorflow_version 1.13.1
import tensorflow as tf
from tensorflow.keras import datasets, layers, models
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix
import matplotlib.pyplot as plt
from pathlib import Path
import os
from PIL import Image
import math
import tensorflow.contrib.layers as ly
from matplotlib.lines import Line2D

# RaceHorses_416x240_30

from google.colab import drive
drive.mount('/content/gdrive')

videoName = 'Horse'
epochs = 30
basePath = Path("/content/gdrive/My Drive/Colab Notebooks/RaceHorses_416x240_30/")
# imgList = [np.array(Image.open(i).convert('RGB')) for i in basePath.iterdir()]

imgList = []
for i in basePath.iterdir():
    if not i.is_dir():
      x = np.array(Image.open(i).convert('RGB'))
      imgList.append(x)



numof_train = int(len(imgList)*0.8)

numof_test = int(len(imgList) - numof_train)

print(numof_train, numof_test)



height = int(len(imgList[0]))
length = int(len(imgList[0][0]))
inputs = (height, length, 3)
N = height * length


train = imgList[:numof_train]  
test = imgList[numof_train:] 

trainSet = np.array(train) 
testSet = np.array(test) 

def cr_1_32():
    # compress
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), padding = "SAME", strides = (4, 4), activation='relu', input_shape=inputs)) 
    model.add(ly.GDN())
    model.add(layers.Conv2D(16, (3, 3),  padding = "SAME", strides = (1, 2), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(8, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(4, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))

    # decompress 
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(8, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(16, (3, 3),   strides = (1, 2), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(32, (3, 3), strides = (4, 4),  padding = "SAME", activation='relu'))

    # convert to 3 color channels 
    model.add(layers.Conv2DTranspose(3, (3, 3),  strides = (1, 1), padding = "SAME", activation='relu'))

    model.summary()

    model.compile(optimizer='adam', loss='mse')
    model.fit(trainSet, trainSet, epochs=epochs)
    prediction = model.predict(testSet)

    psnr = 0
    for i in range(numof_test):
        if i == 19 or i == 39 or i == 59:
                colorlist = []
                for m in range(height):
                  for n in range(length):
                    color = []
                    for c in range(3):
                      color.append( int(prediction[i][m][n][c]) )
                    color = tuple(color)
                    colorlist.append(color)

                newImage = Image.new('RGB', (length, height))
                newImage.putdata(colorlist)
                newImage.save('/content/gdrive/My Drive/Colab Notebooks/output_Img/'+ videoName+"_2_"+str(i)+"_1_32.png")
        img = test[i]
        sum, mse = 0, 0
        for m in range(height):
            for n in range(length):
                for color in range(3):
                    sum += (img[m][n][color] - prediction[i][m][n][color])**2
        mse = sum / (3 * N)
        psnr += 10 * (math.log10(255*255/mse))

    psnr = round(psnr / numof_test, 5)

    return psnr


def cr_1_16():
    # compress
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), padding = "SAME", strides = (4, 4), activation='relu', input_shape=inputs))
    model.add(ly.GDN())
    model.add(layers.Conv2D(16, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(8, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(4, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))

    # decompress 
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(8, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(16, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(32, (3, 3), strides = (4, 4),  padding = "SAME", activation='relu'))

    # convert to 3 color channels 
    model.add(layers.Conv2DTranspose(3, (3, 3),  strides = (1, 1), padding = "SAME", activation='relu'))

    model.summary()


    model.compile(optimizer='adam', loss='mse')
    model.fit(trainSet, trainSet, epochs=epochs)
    prediction = model.predict(testSet)

    psnr = 0
    for i in range(numof_test):
        if i == 19 or i == 39 or i == 59:
              colorlist = []
              for m in range(height):
                for n in range(length):
                  color = []
                  for c in range(3):
                    color.append( int(prediction[i][m][n][c]) )
                  color = tuple(color)
                  colorlist.append(color)

              newImage = Image.new('RGB', (length, height))
              newImage.putdata(colorlist)
              newImage.save('/content/gdrive/My Drive/Colab Notebooks/output_Img/'+ videoName+"_2_"+str(i)+"_1_16.png")

        img = test[i]
        sum, mse = 0, 0
        for m in range(height):
            for n in range(length):
                for color in range(3):
                    sum += (img[m][n][color] - prediction[i][m][n][color])**2
        mse = sum / (3 * N)
        psnr += 10 * (math.log10(255*255/mse))

    psnr = round(psnr / numof_test, 5)
    return psnr


def cr_1_8():
    # compress
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), padding = "SAME", strides = (2, 2), activation='relu', input_shape=inputs)) 
    model.add(ly.GDN())
    model.add(layers.Conv2D(16, (3, 3),  padding = "SAME", strides = (1, 2), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(8, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(4, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))

    # decompress 
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(8, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(16, (3, 3),   strides = (1, 2), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(32, (3, 3), strides = (2, 2),  padding = "SAME", activation='relu'))

    # convert to 3 color channels 
    model.add(layers.Conv2DTranspose(3, (3, 3),  strides = (1, 1), padding = "SAME", activation='relu'))

    
    model.summary()


    model.compile(optimizer='adam', loss='mse')
    model.fit(trainSet, trainSet, epochs=epochs)
    prediction = model.predict(testSet)

    psnr = 0
    for i in range(numof_test):
        if i == 19 or i == 39 or i == 59:
                colorlist = []
                for m in range(height):
                  for n in range(length):
                    color = []
                    for c in range(3):
                      color.append( int(prediction[i][m][n][c]) )
                    color = tuple(color)
                    colorlist.append(color)

                newImage = Image.new('RGB', (length, height))
                newImage.putdata(colorlist)
                newImage.save('/content/gdrive/My Drive/Colab Notebooks/output_Img/'+ videoName+"_2_"+str(i)+"_1_8.png")

        img = test[i]
        sum, mse = 0, 0
        for m in range(height):
            for n in range(length):
                for color in range(3):
                    sum += (img[m][n][color] - prediction[i][m][n][color])**2
        mse = sum / (3 * N)
        psnr += 10 * (math.log10(255*255/mse))


    psnr = round(psnr / numof_test, 5)
    return psnr


def cr_1_4():
    # compress
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), padding = "SAME", strides = (2, 2), activation='relu', input_shape=inputs)) 
    model.add(ly.GDN())
    model.add(layers.Conv2D(16, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(8, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(4, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))

    # decompress 
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(8, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(16, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(32, (3, 3), strides = (2, 2),  padding = "SAME", activation='relu'))

    # convert to 3 color channels 
    model.add(layers.Conv2DTranspose(3, (3, 3),  strides = (1, 1), padding = "SAME", activation='relu'))
    
    model.summary()

    model.compile(optimizer='adam', loss='mse')
    model.fit(trainSet, trainSet, epochs=epochs)
    prediction = model.predict(testSet)


    psnr = 0
    for i in range(numof_test):
        if i == 19 or i == 39 or i == 59:
              colorlist = []
              for m in range(height):
                for n in range(length):
                  color = []
                  for c in range(3):
                    color.append( int(prediction[i][m][n][c]) )
                  color = tuple(color)
                  colorlist.append(color)

              newImage = Image.new('RGB', (length, height))
              newImage.putdata(colorlist)
              newImage.save('/content/gdrive/My Drive/Colab Notebooks/output_Img/'+ videoName+"_2_"+str(i)+"_1_4.png")


        img = test[i]
        sum, mse = 0, 0
        for m in range(height):
            for n in range(length):
                for color in range(3):
                    sum += (img[m][n][color] - prediction[i][m][n][color])**2
        mse = sum / (3 * N)
        psnr += 10 * (math.log10(255*255/mse))

    psnr = round(psnr / numof_test, 5)
    return psnr


def cr_1_2():
    # compress
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), padding = "SAME", strides = (1, 2), activation='relu', input_shape=inputs)) 
    model.add(ly.GDN())
    model.add(layers.Conv2D(16, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(8, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))
    model.add(ly.GDN())
    model.add(layers.Conv2D(4, (3, 3),  padding = "SAME", strides = (1, 1), activation='relu'))

    # decompress 
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(8, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(16, (3, 3),   strides = (1, 1), padding = "SAME", activation='relu'))
    model.add(ly.GDN(inverse=True))
    model.add(layers.Conv2DTranspose(32, (3, 3), strides = (1, 2),  padding = "SAME", activation='relu'))

    # convert to 3 color channels 
    model.add(layers.Conv2DTranspose(3, (3, 3),  strides = (1, 1), padding = "SAME", activation='relu'))

    model.summary()

    model.compile(optimizer='adam', loss='mse')
    model.fit(trainSet, trainSet, epochs=epochs)
    prediction = model.predict(testSet)

    psnr = 0
    for i in range(numof_test):
        if i == 19 or i == 39 or i == 59:
                colorlist = []
                for m in range(height):
                  for n in range(length):
                    color = []
                    for c in range(3):
                      color.append( int(prediction[i][m][n][c]) )
                    color = tuple(color)
                    colorlist.append(color)

                newImage = Image.new('RGB', (length, height))
                newImage.putdata(colorlist)
                newImage.save('/content/gdrive/My Drive/Colab Notebooks/output_Img/'+ videoName+"_2_"+str(i)+"_1_2.png")
        img = test[i]
        sum, mse = 0, 0
        for m in range(height):
            for n in range(length):
                for color in range(3):
                    sum += (img[m][n][color] - prediction[i][m][n][color])**2
        mse = sum / (3 * N)
        psnr += 10 * (math.log10(255*255/mse))


    psnr = round(psnr / numof_test, 5)
    return psnr




def runALL_with_diff_cr():
    r32 = cr_1_32()
    r16 = cr_1_16()
    r8  = cr_1_8()
    r4  = cr_1_4()
    r2  = cr_1_2()


    print("")

    X  = ["1/2", "1/4", "1/8", "1/16", "1/32"]
    Y1 = [r2, r4, r8, r16, r32]

    plt.plot(X, Y1, color= '#1774FF', linewidth = 1, 
            marker='o', markerfacecolor='#1774FF', markersize=7) 


    legend_elements = [Line2D([0], [0], marker='o', color='#1774FF', label= videoName,
                          markerfacecolor='#1774FF', markersize=7)]

    plt.legend(handles=legend_elements, loc='top right')
    plt.xticks(X)
    plt.xlabel('Compression Ratio') 
    plt.ylabel('PSNR') 
    plt.title('Outcome of ' + videoName + ' Video') 
    plt.show()

    print("")
    print("__Method2__")
    print("CR = 1/2, PSNR_with_", epochs, "Epochs = ", r2)
    print("CR = 1/4, PSNR_with_", epochs, "Epochs = ", r4)
    print("CR = 1/8, PSNR_with_", epochs, "Epochs = ", r8)
    print("CR = 1/16, PSNR_with_", epochs, "Epochs = ", r16)
    print("CR = 1/32, PSNR_with_", epochs, "Epochs = ", r32)
    print("")

    return 

    
    
if __name__ == "__main__":
   runALL_with_diff_cr()








