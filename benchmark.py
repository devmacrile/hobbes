import pandas as pd
import seaborn as sns


def make_plots():
	df = pd.read_csv("benchmark_data.csv")
	df['cost'] = 3 * df['reads'] + 3 * df['writes'] + 2 * df['branches'] + (df['instructions'] - df['reads'] - df['writes'] - df['branches'])
	sns.set_theme(style="whitegrid")
	for benchmark in ["benchmark1", "benchmark2", "benchmark3"]:
	    benchmark_df = df.loc[df['benchmark'].isin([benchmark, '{}f'.format(benchmark)])]
	    plot(benchmark_df, "docs/fig/{}.png".format(benchmark))


def plot(df, filename):
    g = sns.catplot(data=df, x="benchmark", y="cost", hue="allocation", kind="bar", palette="dark", height=6, aspect=.6, alpha=.6)
    g.despine(left=True)
    g.set_axis_labels("", "Total Cost")
    g.legend.set_title("")
    g.savefig(filename)

if __name__ == '__main__':
	make_plots()