import pandas as pd
from numpy import *
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression

input = pd.read_csv('pima-indians-diabetes.csv')
numberN = [40, 80, 120, 160, 200]
#dataframe contains only diabetes patients
diabetes = input[input["result"]==1]
#dataframe contains only nondiabetes patients
nondiabetes = input[input["result"]==0]
number_total = len(input.index)
number_diabetes = len(diabetes.index)
number_nondiabetes = len(nondiabetes.index)
res = []
#dataframe contains only 8 parameter values for diabetes patients
x_diabetes = pd.DataFrame(diabetes, columns = diabetes.columns[:-1])
#dataframe contains only 8 parameter values for nondiabetes patients
x_nondiabetes = pd.DataFrame(nondiabetes, columns = nondiabetes.columns[:-1])
y_diabetes = pd.DataFrame(diabetes, columns = ["result"])
y_nondiabetes = pd.DataFrame(nondiabetes, columns = ["result"])

for n in numberN:
    #counter for times of training
    i = 0
    accuracy = 0
    model = LinearRegression()
    #shuffle dataframes for a new train set
    x_diabetes = x_diabetes.sample(frac=1).reset_index(drop=True)
    x_nondiabetes = x_nondiabetes.sample(frac=1).reset_index(drop=True)
    #split train set and test set
    x_diabetes_train = x_diabetes[:n]
    x_nondiabetes_train = x_nondiabetes[:n]
    y_diabetes_train = y_diabetes[:n]
    y_nondiabetes_train = y_nondiabetes[:n]
    x_diabetes_test = x_diabetes[n:]
    x_nondiabetes_test = x_nondiabetes[n:]
    y_diabetes_test = y_diabetes[n:]
    y_nondiabetes_test = y_nondiabetes[n:]
    #combine diabetes train set and nondiabetes train set, do the same for test set
    x_train = pd.concat([x_diabetes_train, x_nondiabetes_train])
    x_test = pd.concat([x_diabetes_test, x_nondiabetes_test])
    y_train = pd.concat([y_diabetes_train, y_nondiabetes_train])
    y_test = pd.concat([y_diabetes_test, y_nondiabetes_test])

    while i < 1000:    
        #train the model
        model.fit(x_train, y_train)
        i += 1

    #test the model
    y_predict = model.predict(x_test)
    correct = 0
    j = 0
    #calculate accuracy
    while j < (number_total - 2*n):
        if y_predict[j] >= 0.5 and j < (number_diabetes - n):
            correct += 1
        if y_predict[j] < 0.5 and j >= (number_diabetes - n):
            correct += 1
        j += 1

    accuracy = round(correct/(number_total-2*n), 4)
    #save accuracy rate for current 2*n data samples
    res.append(accuracy)

print(res)
#draw the plot
X = [80, 160, 240, 320, 400]
Y = res

plt.plot(X, Y, color = 'blue', linestyle = 'solid', linewidth = 2, marker = 'o', markerfacecolor = 'blue', markersize = 10)

plt.xlabel('Number of Samples')
plt.ylabel('Accuracy')
plt.title('Result')
plt.show()
