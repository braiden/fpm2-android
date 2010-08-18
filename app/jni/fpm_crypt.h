/* FIGARO'S PASSWORD MANAGER (FPM)
 * Copyright (C) 2000 John Conneely
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
 * fpm_crypt.h
 */


#include "gpg_trans.h"

#define PBKDF2_ITERATIONS 8192
#define SHA256_DIGEST_LENGTH 32

void fpm_crypt_init(gchar *password);

int (*fpm_setkey) (void *c, const byte *key, unsigned keylen );

gchar* get_new_salt(gint len);

void
fpm_decrypt_field(      void* context,
                        gchar* plaintext,
                        gchar* cipher_field,
                        gint len);
void
fpm_encrypt_field(      void* context,
                        gchar* cipher_field,
                        gchar* plaintext,
                        gint len);

gchar*
fpm_encrypt_field_var(  void* context,
                        gchar* plaintext);

gchar*
fpm_decrypt_field_var(  void* context,
                        gchar* cipher_field);


void fpm_decrypt_all(void);

void fpm_cipher_init(char *cipher);

void fpm_crypt_set_password(gchar *password);

void fpm_decrypt_launchers();

void md5(gchar *prehash, guint8 *hash);

void fpm_sha256_fpm_data(gchar *digest);

gint fpm_sha256_file(gchar *filename, guchar *digest);

gint fpm_pkcs5_pbkdf2(guchar *pass, gsize pass_len, const gchar *salt, gsize salt_len, guint8 *key, gsize key_len, guint rounds);

