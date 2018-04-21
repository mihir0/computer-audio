import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd

gr1 = pd.read_csv("goodRun1.csv")
# gr2 = pd.read_csv("goodRun2.csv")
# gr3 = pd.read_csv("goodRun3.csv")
br1 = pd.read_csv("heelStrike1.csv")

# print("+++++++++++++++++++++++++++++++++")
# print(gr1.std())
# print("+++++++++++++++++++++++++++++++++")
# print(br1.std())

#### good
t = gr1.iloc[:, 0]
x = gr1.iloc[:, 1]
y = gr1.iloc[:, 2]
z = gr1.iloc[:, 3]

t = t / 1000

plt.plot(t, x)
plt.plot(t, y)
plt.plot(t, z)

squared = x + z
# plt.plot(t, squared)
#
# plt.show()

#### bad
t1 = br1.iloc[:, 0]
x1 = br1.iloc[:, 1]
y1 = br1.iloc[:, 2]
z1 = br1.iloc[:, 3]

t1 = t1 / 1000

# plt.plot(t1, x1)
# plt.plot(t1, y1)
# plt.plot(t1, z1)

# squared = x1 + z1
# plt.plot(t1, squared)


plt.close('all')

# row and column sharing
f, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, sharex='col', sharey='row')
ax1.plot(t, x, color=((0.2, 0, 0.2)))
ax1.set_title('x versus t')
ax2.scatter(t, y, color=((0.2, 0, 0.2)))
ax2.set_title('y versus t')
ax3.scatter(t, z, color=((0.2, 0, 0.2)))
ax3.set_title('z versus t')
ax4.plot(t, z * 2, color=((0.5, 0, 1)))
ax4.set_title('z * 2 versus t')
