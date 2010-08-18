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
 *
 * gpg_trans.h
 * I use blowfish.c, rijndael.c and sha256.c from GNU PG. This header file
 * is a hack to get it to compile.
 */

#include <gtk/gtk.h>

/* From types.h: */
typedef guint32 u32; /* GPG used complex method to ensure 32 bit unsigned int.
		      * Since this is a gnome app, we can take use guint32!
		      */
typedef guchar byte;

/* From errors.h: */
#define G10ERR_SELFTEST_FAILED 50
#define G10ERR_WEAK_KEY       43
#define G10ERR_WRONG_KEYLEN   44

/* Using this instead of memset() to prevent possible optimalization. Taken from gnupg util.h */
#define wipememory2(_ptr,_set,_len) do { volatile char *_vptr=(volatile char *)(_ptr); size_t _vlen=(_len); while(_vlen) { *_vptr=(_set); _vptr++; _vlen--; } } while(0)
#define wipememory(_ptr,_len) wipememory2(_ptr,0,_len)

const char *
blowfish_get_info( int algo, size_t *keylen,
                   size_t *blocksize, size_t *contextsize,
                   int  (**setkeyf)( void *c, const byte *key, unsigned keylen ),
                   void (**encryptf)( void *c, byte *outbuf, const byte *inbuf ),
                   void (**decryptf)( void *c, byte *outbuf, const byte *inbuf )
                 );

const char *
rijndael_get_info( int algo, size_t *keylen,
		   size_t *blocksize, size_t *contextsize,
		   int (**setkeyf)( void *c, const byte *key, unsigned keylen),
		   void (**encryptf)(void *c, byte *outbuf, const byte *inbuf),
		   void (**decryptf)(void *c, byte *outbuf, const byte *inbuf)
		   );

