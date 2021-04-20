import gzip
import re
import pathlib
import ntpath


def parse(filename):
    f = open(filename, 'r') if pathlib.Path(filename).suffix == ".txt" else gzip.open(filename, 'r')
    entry = {}
    for l in f:
        l = l.strip()
        colonPos = l.find(':')
        if colonPos == -1:
            yield entry
            entry = {}
            continue
        eName = l[:colonPos]
        rest = l[colonPos + 2:]
        if eName == "review/helpfulness":
            entry["numerator"] = int(rest.split('/')[0])
            entry["denominator"] = int(rest.split('/')[1])
        else:
            entry[eName] = rest
    yield entry


def count_tokens(text: str):
    total = 0
    diff = set()
    max_len = 0
    text = text.lower()
    for token in re.compile("[^a-z0-9]").split(text):
        if token:
            total += 1
            diff.add(token)
            max_len = max(max_len, len(token))

    return total, diff, max_len


if __name__ == '__main__':
    datasets = [
        r"C:\Users\Ido\Documents\Degree\Third Year\Semester B\Web Information Retrival\webdata\datasets\1000.txt",
        r"C:\Users\Ido\Documents\Degree\Third Year\Semester B\Web Information Retrival\webdata\datasets\100.txt"]

    for dataset in datasets:
        all_products = set()
        all_tokens = set()
        total_num_of_tokens = 0
        max_token_length = 0
        max_tokens_in_review = 0
        max_denominator = 0
        max_numerator = 0
        num_of_reviews = 0

        for e in parse(dataset):
            if not e:
                break
            num_of_reviews += 1
            all_products.add(e["product/productId"])

            max_denominator = max(max_denominator, e["denominator"])
            max_numerator = max(max_denominator, e["numerator"])

            num_of_tokens_in_e, tokens_in_e, max_token_len_in_e = count_tokens(e["review/text"])
            total_num_of_tokens += num_of_tokens_in_e
            all_tokens = all_tokens.union(tokens_in_e)
            max_token_length = max(max_token_length, max_token_len_in_e)
            max_tokens_in_review = max(max_tokens_in_review, num_of_tokens_in_e)

        num_of_products = len(all_products)
        num_of_diff_tokens = len(all_tokens)

        print(f"Statistics for - {ntpath.basename(dataset)}:\n"
              f"num of reviews - {num_of_reviews}\n"
              f"num of products - {num_of_products}\n"
              f"total num of tokens - {total_num_of_tokens}\n"
              f"num of different tokens - {num_of_diff_tokens}\n"
              f"max token len - {max_token_length}\n"
              f"max num of tokens in review - {max_tokens_in_review}\n"
              f"max numerator - {max_numerator}\n"
              f"max denominator - {max_denominator}\n")
