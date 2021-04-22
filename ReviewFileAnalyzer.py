import gzip
import re
import pathlib
import ntpath
import json


#
# def parse_json(filename):
#     f = gzip.open(filename, 'r')
#     for review in f:
#         review = review.decode("utf-8")
#         yield json.loads(review)


def parse(filename):
    is_bytes = pathlib.Path(filename).suffix == ".gz"
    f = gzip.open(filename, 'r') if is_bytes else open(filename, 'r')
    entry = {}
    for l in f:
        if is_bytes:
            try:
                l = l.decode("utf-8")
            except:
                print(f"failed to decode {l}")
                continue
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
    max_len = 0
    text = text.lower()
    for token in re.compile("[^a-z0-9]").split(text):
        if token:
            total += 1
            all_tokens[token] = all_tokens.get(token, 0) + 1
            max_len = max(max_len, len(token))

    return total, max_len


if __name__ == '__main__':
    datasets = [
        r"datasets\100.txt",
        r"datasets\1000.txt"
        # r"datasets\Books.txt.gz"
    ]

    for dataset in datasets:
        all_tokens = {}
        all_products = {}
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
            all_products[e["product/productId"]] = all_products.get(e["product/productId"], 0) + 1

            max_denominator = max(max_denominator, e["denominator"])
            max_numerator = max(max_denominator, e["numerator"])

            num_of_tokens_in_e, max_token_len_in_e = count_tokens(e["review/text"])
            total_num_of_tokens += num_of_tokens_in_e
            max_token_length = max(max_token_length, max_token_len_in_e)
            max_tokens_in_review = max(max_tokens_in_review, num_of_tokens_in_e)

        num_of_products = len(all_products.keys())
        num_of_diff_tokens = len(all_tokens.keys())
        most_popular_token = max(all_tokens, key=all_tokens.get)
        max_token_frq = all_tokens[most_popular_token]
        most_popular_product = max(all_products, key=all_products.get)
        max_product_freq = all_products[most_popular_product]

        print(f"Statistics for - {ntpath.basename(dataset)}:\n"
              f"num of reviews - {num_of_reviews}\n"
              f"num of products - {num_of_products}\n"
              f"total num of tokens - {total_num_of_tokens}\n"
              f"num of different tokens - {num_of_diff_tokens}\n"
              f"most popular product - {most_popular_product}\n"
              f"max product freq - {max_product_freq}\n"
              f"most popular token - {most_popular_token}\n"
              f"max token freq - {max_token_frq}\n"
              f"max token len - {max_token_length}\n"
              f"max num of tokens in review - {max_tokens_in_review}\n"
              f"max numerator - {max_numerator}\n"
              f"max denominator - {max_denominator}\n")
