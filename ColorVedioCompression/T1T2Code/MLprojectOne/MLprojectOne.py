#Q1 Need check for recognition of accuracy rate and confusion matrix
import tensorflow as tf
from keras import datasets, layers, models 
import numpy as np

fashion_mnist = datasets.fashion_mnist
(train_images, train_labels), (test_images, test_labels) = fashion_mnist.load_data()

train_images = train_images.reshape((60000, 28, 28, 1))
test_images = test_images.reshape((10000, 28, 28, 1))

# Normalize pixel values to be between 0 and 1
train_images, test_images = train_images / 255.0, test_images / 255.0

model = models.Sequential()
model.add(layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1))) 
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(64, (3, 3), activation='relu'))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(64, (3, 3), activation='relu'))
model.add(layers.Flatten())
model.add(layers.Dense(64, activation='relu'))
model.add(layers.Dense(10, activation='softmax'))

model.summary()

model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

model.fit(train_images, train_labels, epochs=5,batch_size=32)

test_loss, test_acc = model.evaluate(test_images, test_labels)

print(test_acc)

y_test_hat_mat = model.predict(test_images)
y_test_hat = np.argmax(y_test_hat_mat, axis=1)
from sklearn.metrics import confusion_matrix
cm=confusion_matrix(test_labels, y_test_hat, labels=range(10))
print(cm)



#Q2
import tensorflow as tf
from keras import datasets, layers, models 
import numpy as np
import matplotlib.pyplot as plt
import math

fashion_mnist = datasets.fashion_mnist 
(train_images, train_labels), (test_images, test_labels) = fashion_mnist.load_data() 
train_images = train_images / 255.0
test_images = test_images / 255.0
p = [10, 50, 200]
fig = plt.figure()
for i in range(1,11):
    fig.add_subplot(4,10,i)
    plt.imshow(test_images[i-1])
    plt.gray()
    plt.xticks([])
    plt.yticks([])

def train_model(p_index):
    model = models.Sequential()
    model.add(layers.Flatten())
    model.add(layers.Dense(p[p_index]))
    model.add(layers.Dense(28*28*2, activation='relu'))
    model.add(layers.Dense(28*28))
    model.add(layers.Reshape((28,28)))
    #model.summary()

    model.compile(optimizer='adam',
                  loss='mean_squared_error',
                  metrics=['accuracy'])

    model.fit(train_images, train_images, epochs=5,batch_size=64)

    psnr = 0
    prediction = model.predict(test_images)

    for i in range(10000):
        im = test_images[i]
        sum = 0
        for m in range(28):
            for n in range(28):
                sum = sum + (im[m][n] - prediction[i][m][n])**2
        mse = sum/(28*28)
        psnr = psnr + 10*(math.log10(1/mse))

    psnr = round(psnr / 10000, 5)
    print("P=", p[p_index], ", PSNR=", psnr)

    for i in range(1,11):
        fig.add_subplot(4,10,i+(p_index+1)*10)
        plt.imshow(prediction[i-1])
        plt.gray()
        plt.xticks([])
        plt.yticks([])

for i in range(3):
    train_model(i)

plt.show()
