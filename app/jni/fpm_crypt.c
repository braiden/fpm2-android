/* FIGARO'S PASSWORD MANAGER 2 (FPM2)
 * Copyright (C) 2000 John Conneely
 * Copyright (C) 2009 Aleš Koval
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
 * This file contains the crypto interface for FPS.
 *
 * fpm_crypt.c - Crypto interface for FPM
 */

#include <gtk/gtk.h>
#include <stdio.h>
#include <stdlib.h>
#include "fpm_crypt.h"
#include "fpm.h"

static fpm_cipher ciphers[] = {
    { "BLOWFISH", 16, 4 },
    { "AES-256", 32, 16 }
};

void (*fpm_encrypt_block) (void *c, byte *outbuf, const byte *inbuf);
void (*fpm_decrypt_block) (void *c, byte *outbuf, const byte *inbuf);

static size_t keylen, blocksize, contextsize;

static void fpm_hex_to_bin(byte* out, const gchar* in, gint len);
static void fpm_bin_to_hex(gchar* out, const byte* in, gint len);

void fpm_cipher_init(char *cipher_name) {

    if(strcmp(cipher_name, "BLOWFISH") == 0) {
	cipher_algo = BLOWFISH;
    }
    else if(strcmp(cipher_name, "AES-256") == 0) {
	cipher_algo = AES256;
    }

    switch(cipher_algo)  {
	case BLOWFISH:
		if (strcmp("BLOWFISH", blowfish_get_info(4, &keylen, &blocksize,
					     &contextsize, &fpm_setkey,
					     &fpm_encrypt_block,
					     &fpm_decrypt_block)))
		{ g_assert_not_reached(); }
		break;
	case AES256:
		if (strcmp("AES256", rijndael_get_info(9, &keylen, &blocksize,
					     &contextsize, &fpm_setkey,
					     &fpm_encrypt_block,
					     &fpm_decrypt_block)))
		{ g_assert_not_reached(); }
		break;
	default:
		printf("Unknown cipher algorithm!\n");
		exit(-1);
    }
    cipher->name = ciphers[cipher_algo].name;
    cipher->hash_len = ciphers[cipher_algo].hash_len;
    cipher->salt_len = ciphers[cipher_algo].salt_len;
    cipher->keylen = keylen;
    cipher->blocksize = blocksize;
    cipher->contextsize = contextsize;
}

void fpm_crypt_init(gchar* password) {
  gchar *prehash1 = "", *prehash2 = "";
  byte *hash_1, *hash_2;

  old_context = g_malloc(cipher->contextsize);
  new_context = g_malloc(cipher->contextsize);

  hash_1 = g_malloc(cipher->hash_len);
  hash_2 = g_malloc(cipher->hash_len);

  if(cipher_algo == AES256) {
    fpm_pkcs5_pbkdf2((guchar *)password, strlen(password), old_salt, cipher->salt_len, hash_1, cipher->hash_len, PBKDF2_ITERATIONS);
    fpm_pkcs5_pbkdf2((guchar *)password, strlen(password), new_salt, cipher->salt_len, hash_2, cipher->hash_len, PBKDF2_ITERATIONS);
  } else {
    prehash1 = g_strdup_printf("%s%s", old_salt, password);
    prehash2 = g_strdup_printf("%s%s", new_salt, password);

    md5(prehash1, hash_1);
    md5(prehash2, hash_2);
  }

  fpm_setkey(old_context, hash_1, cipher->hash_len);
  fpm_setkey(new_context, hash_2, cipher->hash_len);

  wipememory(hash_1, cipher->hash_len);
  wipememory(hash_2, cipher->hash_len);

  g_free(hash_1);
  g_free(hash_2);

  if (cipher_algo == BLOWFISH) {
    wipememory(prehash1, strlen(prehash1));
    wipememory(prehash2, strlen(prehash2));
    g_free(prehash1);
    g_free(prehash2);
  }
}

void fpm_crypt_set_password(gchar *password) {
    gchar *prehash = "";
    byte *hash;

    new_salt = get_new_salt(cipher->salt_len);
    hash = g_malloc(cipher->hash_len);

    if(cipher_algo == AES256) {
	fpm_pkcs5_pbkdf2((guchar *)password, strlen(password), new_salt, cipher->salt_len, hash, cipher->hash_len, PBKDF2_ITERATIONS);
    } else {
	prehash = g_strdup_printf("%s%s", new_salt, password);
	md5(prehash, hash);
    }

    fpm_setkey(new_context, hash, cipher->hash_len);

    if (cipher_algo == BLOWFISH)
	wipememory(prehash, strlen(prehash));

    wipememory(hash, cipher->hash_len);
    g_free(hash);
}

static void
fpm_addnoise(gchar* field, gint len)
{
  /* If we have a short string, I add noise after the first null prior
   * to encrypting.  This prevents empty blocks from looking identical
   * to eachother in the encrypted file.  rnd() is probably good enough
   * for this... no need to decrease entrophy in /dev/random.
   */
  gint i;
  gboolean gotit=FALSE;

  for(i=0;i<len;i++)
    if (gotit)
    {
      field[i]=(char)(256.0*rand()/(RAND_MAX+1.0));
    }
    else if (field[i]=='\00')
    {
        gotit=TRUE;
    }
}

static void
fpm_rotate(gchar* field, gint len)
{
  /* After we use addnoise (above) we ensure blocks don't look identical
   * unless all 8 chars in the block are part of the password.  This
   * routine makes us use all three blocks equally rather than fill the
   * first, then the second, etc.   This makes it so none of the blocks
   * in the password will remain constant from save to save, even if the
   * password is from 7-20 characters long.  Note that passwords from
   * 21-24 characters start to fill blocks, and so will be constant.
   */

  gint num_blocks;
  gchar* tmp;
  gint b, i;

  g_assert(blocksize>0);
  num_blocks = len/blocksize;

  g_assert(len==num_blocks*blocksize);
  tmp=g_malloc0(len+1);
  for(b=0;b<num_blocks;b++)
  {
    for(i=0;i<blocksize;i++) tmp[b*blocksize+i] = field[i*num_blocks+b];
  }
  memcpy(field, tmp, len);
  wipememory(tmp, len);
  g_free(tmp);
}

static void
fpm_unrotate(gchar* field, gint len)
{
  gint num_blocks;
  gchar* tmp;
  gint b, i;

  g_assert(blocksize>0);
  num_blocks = len/blocksize;
  g_assert(len==num_blocks*blocksize);
  tmp=g_malloc0(len+1);
  for(b=0;b<num_blocks;b++)
  {
    for(i=0;i<blocksize;i++) tmp[i*num_blocks+b] = field[b*blocksize+i];
  }
  memcpy(field, tmp, len);
  wipememory(tmp, len);
  g_free(tmp);
}

void
fpm_decrypt_field(	void* context,
		  	gchar* plaintext,
			gchar* cipher_field,
			gint len)
{
  gint num_blocks;
  gint i;
  byte* ciphertext;

  g_assert(strlen(cipher_field)==2*len);
  g_assert(blocksize>0);

  /* Calculate # of blocks */
  num_blocks = len / blocksize;

  /* We assume len is multiple of blocksize */
  g_assert((len-num_blocks*blocksize)==0);

  /* Decode cipher_field to chphertext */
  ciphertext = g_malloc(len);
  fpm_hex_to_bin(ciphertext, cipher_field, len);

  for(i=0;i<num_blocks; i++)
  {
    fpm_decrypt_block(context,
	 (byte *)(plaintext+i*blocksize), (ciphertext+i*blocksize) );
  }
  fpm_unrotate(plaintext, len);

  g_free(ciphertext);
}

gchar*
fpm_decrypt_field_var(	void* context,
			gchar* cipher_field)
{
  gint len, num_blocks;
  gchar* plaintext;


  len = strlen(cipher_field);
  num_blocks = len/blocksize/2;

  g_assert(num_blocks * 2 * blocksize == len);

  len = len / 2;

  plaintext = g_malloc0(len+1); 

  fpm_decrypt_field(context, plaintext, cipher_field, len); 

  return(plaintext);
}

void
fpm_encrypt_field(	void* context,
			gchar* cipher_field,
			gchar* plaintext,
			gint len)
{
  gint i;
  gint num_blocks;
  byte* ciphertext;

  g_assert(blocksize>0);

  fpm_addnoise(plaintext, len);
  fpm_rotate(plaintext, len);

  /* Calculate # of blocks */
  num_blocks = len / blocksize;

  ciphertext = g_malloc(len);

  for(i=0; i<num_blocks; i++)
  {
    fpm_encrypt_block(context,
	 (ciphertext+i*blocksize), (byte *)(plaintext+i*blocksize) );
  }

  fpm_bin_to_hex(cipher_field, ciphertext, len);
  g_free(ciphertext);
}

gchar*
fpm_encrypt_field_var(	void* context,
                        gchar* plaintext)
{
  gint num_blocks, len;
  gchar* cipher_field;
  gchar* plain_field;

  num_blocks = (strlen(plaintext)/(blocksize-1))+1;
  len = num_blocks*blocksize;
  plain_field = g_malloc0(len+1);
  strncpy(plain_field, plaintext, len);
  cipher_field = g_malloc0((len*2)+1);

  fpm_encrypt_field(context, cipher_field, plain_field, num_blocks*blocksize);
  wipememory(plain_field, len);
  g_free(plain_field);

  return(cipher_field);
}

static void fpm_hex_to_bin(byte* out, const gchar* in, gint len)
{
  gint i, high, low;
  byte data;

  for(i=0; i<len; i++)
  {
    high=in[2*i]-'a';
    low=in[2*i+1]-'a';
    data = high*16+low;
    out[i]=data;
  }
}

static void fpm_bin_to_hex(gchar* out, const byte* in, gint len)
{
  gint i, high, low;
  byte data;

  for(i=0; i<len; i++)
  {
    data = in[i];
    high=data/16;
    low = data - high*16;
    out[2*i]='a'+high;
    out[2*i+1]='a'+low;
  }
}

gchar* get_new_salt(gint len)
{
  byte* data;
  gchar* ret_val;
  FILE* rnd;
  size_t result;

  data = g_malloc0(len+1);
  ret_val = g_malloc0(len*2+1);
  rnd=fopen("/dev/random", "r");

  result = fread(data, 1, len, rnd);
  fpm_bin_to_hex(ret_val, data, len);
  fclose(rnd);
  g_free(data);
  return(ret_val);
}

void static fpm_decrypt_field_inplace(gchar** text_ptr)
{
  gchar* old_text;
  gchar* new_text;

  old_text = *text_ptr;
  new_text = fpm_decrypt_field_var(old_context, old_text);
  g_free(old_text);
  *text_ptr=new_text;
}


void fpm_decrypt_all()
{
  GList *list;
  fpm_data *data;

  list = g_list_first(glb_pass_list);
  while(list != NULL)
  {
    data = list->data;
    fpm_decrypt_field_inplace(&data->title);
    fpm_decrypt_field_inplace(&data->arg);
    fpm_decrypt_field_inplace(&data->user);
    fpm_decrypt_field_inplace(&data->notes);
    fpm_decrypt_field_inplace(&data->category);
    fpm_decrypt_field_inplace(&data->launcher);

    list=g_list_next(list);
  }
}

void fpm_decrypt_launchers() {
  GList *list;
  fpm_launcher *data;
  gchar* plaintext;

  list = g_list_first(glb_launcher_list);
  while(list != NULL)
  {
    data = list->data;
    fpm_decrypt_field_inplace(&data->title);
    fpm_decrypt_field_inplace(&data->cmdline);

    plaintext = fpm_decrypt_field_var(old_context, data->c_user);
    data->copy_user = atoi(plaintext);

    plaintext = fpm_decrypt_field_var(old_context, data->c_pass);
    data->copy_password = atoi(plaintext);
    g_free(data->c_pass);

    list = g_list_next(list);
  }
}

void md5(gchar *prehash, guint8 *hash) {
	GChecksum *checksum;
	gsize size = cipher->hash_len;

	checksum = g_checksum_new(G_CHECKSUM_MD5);
	g_checksum_update(checksum, (guchar *)prehash, strlen(prehash));
	g_checksum_get_digest(checksum, hash, &size);
	g_checksum_free(checksum);
}
