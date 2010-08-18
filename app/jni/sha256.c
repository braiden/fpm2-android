/* FIGARO'S PASSWORD MANAGER 2 (FPM2)
 * Copyright (C) 2009-2010 Aleš Koval
 *
 * FPM is open source / free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FPM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 */

/* pkcs5_pbkdf2 function is taken from bioctl package
 *
 * Copyright (c) 2008 Damien Bergamini <damien.bergamini@free.fr>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

/*  Test vectors from FIPS-180-2:
 *
 *  "abc"
 * 224:
 *  23097D22 3405D822 8642A477 BDA255B3 2AADBCE4 BDA0B3F7 E36C9DA7
 * 256:
 *  BA7816BF 8F01CFEA 414140DE 5DAE2223 B00361A3 96177A9C B410FF61 F20015AD
 *
 *  "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
 * 224:
 *  75388B16 512776CC 5DBA5DA1 FD890150 B0C6455C B4F58B19 52522525
 * 256:
 *  248D6A61 D20638B8 E5C02693 0C3E6039 A33CE459 64FF2167 F6ECEDD4 19DB06C1
 *
 *  "a" x 1000000
 * 224:
 *  20794655 980C91D8 BBB4C1EA 97618A4B F03F4258 1948B2EE 4EE7AD67
 * 256:
 *  CDC76E5C 9914FB92 81A1C7E2 84D73E67 F1809A48 A497200E 046D39CC C7112CD0
 */

#include "fpm.h"
#include "fpm_crypt.h"

typedef struct {
  GChecksum *outer, *inner;
} fpm_hmacsha256;

static void fpm_hmacsha256_init(fpm_hmacsha256 *c, guchar *key, gint keylen) {
  guchar realkey[64], outerkey[64], innerkey[64];
  GChecksum *keyc;
  gsize size = SHA256_DIGEST_LENGTH;
  gint i;

  memset(realkey, 0, sizeof(realkey));
  if(keylen > 64) {
    keyc = g_checksum_new(G_CHECKSUM_SHA256);
    g_checksum_update(keyc, key, keylen);
    g_checksum_get_digest(keyc, realkey, &size);
    g_checksum_free(keyc);
    keylen = 32;
  } else {
    memcpy(realkey, key, keylen);
  }

  /* abusing the cache here, if we do sha256 in between that'll erase it */
  for(i = 0; i<64 ; i++) {
    int r = realkey[i];
    innerkey[i] = r ^ 0x36;
    outerkey[i] = r ^ 0x5c;
  }
    c->outer = g_checksum_new(G_CHECKSUM_SHA256);
    c->inner = g_checksum_new(G_CHECKSUM_SHA256);
    g_checksum_update(c->outer, outerkey, 64);
    g_checksum_update(c->inner, innerkey, 64);
}

static void fpm_hmacsha256_write(fpm_hmacsha256 *c, guchar *message, gint messagelen) {
    g_checksum_update(c->inner, message, messagelen);
}

static void fpm_hmacsha256_final(fpm_hmacsha256 *c, guchar *digest) {
  gsize size = SHA256_DIGEST_LENGTH;
  g_checksum_get_digest(c->inner, digest, &size);
  g_checksum_free(c->inner);
  g_checksum_update(c->outer, digest, SHA256_DIGEST_LENGTH);
  g_checksum_get_digest(c->outer, digest, &size);
  g_checksum_free(c->outer);
}

void fpm_hmac_sha256(guchar *key, gint key_len, guchar *data, gint data_len, guchar *mac) {
  fpm_hmacsha256 hmac;
  guchar digest[SHA256_DIGEST_LENGTH];

  fpm_hmacsha256_init(&hmac, key, key_len);
  fpm_hmacsha256_write(&hmac, data, data_len);
  fpm_hmacsha256_final(&hmac, digest);

  memcpy(mac, digest, SHA256_DIGEST_LENGTH);
  wipememory(digest, sizeof(digest));
}

/*
 *   Password-Based Key Derivation Function 2 (PKCS #5 v2.0).
 *   Code based on IEEE Std 802.11-2007, Annex H.4.2.
*/

gint fpm_pkcs5_pbkdf2(guchar *pass, gsize pass_len, const gchar *salt, gsize salt_len,
    guint8 *key, gsize key_len, guint rounds)
{
	guint8 *asalt, obuf[SHA256_DIGEST_LENGTH];
	guint8 d1[SHA256_DIGEST_LENGTH], d2[SHA256_DIGEST_LENGTH];
	guint i, j;
	guint count;
	gsize r;

	if (rounds < 1 || key_len == 0)
		return -1;
	if (salt_len == 0 || salt_len > 256 - 1)
		return -1;
	if ((asalt = g_malloc(salt_len + 4)) == NULL)
		return -1;

	memcpy(asalt, salt, salt_len);

	for (count = 1; key_len > 0; count++) {
		asalt[salt_len + 0] = (count >> 24) & 0xff;
		asalt[salt_len + 1] = (count >> 16) & 0xff;
		asalt[salt_len + 2] = (count >> 8) & 0xff;
		asalt[salt_len + 3] = count & 0xff;
		fpm_hmac_sha256(pass, pass_len, asalt, salt_len + 4, d1);
		memcpy(obuf, d1, sizeof(obuf));
		for (i = 1; i < rounds; i++) {
			fpm_hmac_sha256(pass, pass_len, d1, sizeof(d1), d2);
			memcpy(d1, d2, sizeof(d1));
			for (j = 0; j < sizeof(obuf); j++)
				obuf[j] ^= d1[j];
		}

		r = MIN(key_len, SHA256_DIGEST_LENGTH);
		memcpy(key, obuf, r);
		key += r;
		key_len -= r;
	};
	wipememory(asalt, salt_len + 4);
	g_free(asalt);
	wipememory(d1, sizeof(d1));
	wipememory(d2, sizeof(d2));
	wipememory(obuf, sizeof(obuf));

	return 0;
}

/* Calculate sha256 hash of file, return 0 for success */
gint fpm_sha256_file(gchar *filename, guchar *digest) {
    FILE *fp;
    GChecksum *checksum;
    gsize size = SHA256_DIGEST_LENGTH;
    guchar buf[1024];
    gulong br;
    gint ret_val = -1;

    checksum = g_checksum_new(G_CHECKSUM_SHA256);

    if((fp = fopen(filename, "r")) != NULL) {
	while ((br = fread (buf, sizeof (guchar), 1024, fp)) > 0) {
	    g_checksum_update(checksum, buf, br);
        }
	if(!ferror(fp)) {
	    g_checksum_get_digest(checksum, digest, &size);
	    g_checksum_free(checksum);
	    ret_val = 0;
	}
        fclose(fp);
    }

    return(ret_val);
}

/* Calculate sha256 hash from all fpm_data entries except passwords */
void fpm_sha256_fpm_data(gchar *digest) {
  GChecksum *checksum;
  gsize size = SHA256_DIGEST_LENGTH;
  GList *list;
  fpm_data *data;
  gchar *buf;

  checksum = g_checksum_new(G_CHECKSUM_SHA256);

  list = g_list_first(glb_pass_list);
  while(list != NULL) {
    data = list->data;
    buf = g_strdup_printf("%s%s%s%s%s%s", data->title, data->arg,
	data->user, data->notes, data->category, data->launcher);
    g_checksum_update(checksum, (guchar *)buf, strlen(buf));
    list = g_list_next(list);
    g_free(buf);
  }
  g_checksum_get_digest(checksum, (guchar *)digest, &size);
  g_checksum_free(checksum);
}
