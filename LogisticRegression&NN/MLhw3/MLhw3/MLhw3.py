#Q1 Code
import tensorflow as tf
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix

mnist = tf.keras.datasets.mnist
(x_traino, y_train), (x_testo, y_test) = mnist.load_data()
x_train = np.reshape(x_traino, (60000, 28*28))
x_test = np.reshape(x_testo, (10000, 28*28))
x_train = x_train/255.0
x_test = x_test/255.0
logreg = LogisticRegression(solver='saga', multi_class='multinomial', max_iter=100,verbose=2)
logreg.fit(x_train, y_train)
y_predict = logreg.predict(x_test)
num_correct = 0
for i in range(len(y_test)):
    if y_predict[i]==y_test[i]:
        num_correct +=1        
Accuracy_rate = num_correct/len(y_test)
print("Accuracy Rate = ", Accuracy_rate)

cm = confusion_matrix(y_test, y_predict, labels=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
print(cm)

#Q2 Code
import keras
from keras.models import Sequential
from keras.layers import Dense
from keras.datasets import mnist
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn import metrics
from sklearn.metrics import confusion_matrix

(x_train, y_train), (x_test, y_test) = mnist.load_data()
image_vector_size = 28*28
x_train = x_train.reshape(x_train.shape[0], image_vector_size)/255.0
x_test = x_test.reshape(x_test.shape[0], image_vector_size)/255.0
y_train = keras.utils.to_categorical(y_train, 10)

# create model
model = Sequential()
model.add(Dense(512, input_dim=28*28,  activation='relu'))
model.add(Dense(10,  activation='softmax'))
# Compile model
from keras import optimizers
adam = optimizers.Adam(lr=0.001, beta_1=0.9, beta_2=0.999, amsgrad=False)
model.compile(loss='binary_crossentropy', optimizer=adam, metrics=['categorical_accuracy'])

# Fit the model
model.fit(x_train, y_train, epochs=5, batch_size=32)
# calculate predictions
predictions = model.predict(x_test) # y
# round predictions
y_predict = np.argmax(predictions, axis=1)

num_correct = 0
for i in range(len(y_test)):
    if y_predict[i]==y_test[i]:
        num_correct +=1
        
Accuracy_rate = num_correct/len(y_test)
print("Accuracy Rate = ", Accuracy_rate)

cm = confusion_matrix(y_test, y_predict, labels=[0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
print(cm)




