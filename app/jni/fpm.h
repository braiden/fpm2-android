/* FIGARO'S PASSWORD MANAGER 2 (FPM2)
 * Copyright (C) 2000 John Conneely
 * Copyright (C) 2008-2010 Aleš Koval
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
 * fpm.h
 */

#ifdef HAVE_CONFIG_H
#  include <config.h>
#endif

#ifdef FPM_DEBUG
#   define GTK_DISABLE_DEPRECATED
#endif

#include <gtk/gtk.h>
#include <glib.h>
#include <glib/gi18n.h>

/* CONSTANTS */
#define FPM_NUM_COLS 	 8	 /* Number of all available columns */
#define FPM_DATA_POINTER	 0
#define FPM_TITLE	 1
#define FPM_URL		 2
#define FPM_USER	 3
#define FPM_PASSWORD	 4
#define FPM_CATEGORY	 5
#define FPM_NOTES	 6
#define FPM_LAUNCHER	 7

enum {  ACTION_RUN_LAUNCHER,
	ACTION_COPY_USERNAME,
	ACTION_COPY_PASSWORD,
	ACTION_COPY_BOTH,
	ACTION_EDIT_ENTRY
};

#define FPM_MAX_DOUBLE_CLICK_TIME 0.8 /* In seconds */

#define FPM_PASSWORD_LEN 256

#define SELECTION_PRIMARY	0
#define SELECTION_CLIPBOARD	1

#define DISPLAY_VERSION 	"0.75"
#define FULL_VERSION 		"00.75.00"
#define MIN_VERSION		"00.75.00"
#define MIN_VERSION_BLOWFISH	"00.58.00"
#define ENCRYPT_VERSION		"00.23.00"

#define FPM_ALL_CAT_MSG		_("<ALL CATEGORIES>")
#define FPM_DEFAULT_CAT_MSG	_("<DEFAULTS>")
#define FPM_NONE_CAT_MSG	C_("category","<NONE>")

#define FPM_DIR			".fpm"

/* Using this instead of memset() to prevent possible optimalization. Taken from gnupg util.h */
#define wipememory2(_ptr,_set,_len) do { volatile char *_vptr=(volatile char *)(_ptr); size_t _vlen=(_len); while(_vlen) { *_vptr=(_set); _vptr++; _vlen--; } } while(0)
#define wipememory(_ptr,_len) wipememory2(_ptr,0,_len)

/* STRUCTURES */
typedef struct fpm_data_struct
{
  gchar *title;
  gchar *arg;
  gchar *user;
  gchar *notes;
  gchar *launcher;
  gchar password[FPM_PASSWORD_LEN*2+1];
  gchar *category;
  gint default_list;
} fpm_data;


typedef struct fpm_launcher_struct
{
  gchar *title;
  gchar *cmdline;
  gchar *c_user;
  gchar *c_pass;
  gint copy_user;
  gint copy_password;
} fpm_launcher;

extern gchar *columns_title[FPM_NUM_COLS];
extern GList *columns_order;

typedef struct fpm_ini_struct
{
    gboolean save_on_quit;
    gboolean save_on_change;
    gboolean save_on_add;
    gboolean save_on_delete;
    gboolean create_backup;
    gint number_backup_files;
    gchar *last_category;
    gint main_x;
    gint main_y;
    gint main_width;
    gint main_height;
    gint columns_width[FPM_NUM_COLS];
    gboolean search_match_case;
    gboolean search_in_title;
    gboolean search_in_url;
    gboolean search_in_user;
    gboolean search_in_notes;
    gboolean search_limit_category;
    gboolean enable_tray_icon;
    gboolean tr_always_visible;
    gboolean tr_auto_hide;
    gint tr_auto_hide_minutes;
    gboolean tr_auto_lock;
    gint tr_auto_lock_minutes;
    gint toolbar_visible;
    gint toolbar_icon_style;
    gboolean startup_category_active;
    gchar *startup_category;
    gint copy_target;
    gboolean clear_target;
    gboolean after_unhide_set_startup_category;
    gboolean dont_remember_position;
    gint dblclick_action;
    gboolean startup_search;
} fpm_ini;

typedef struct fpm_state_struct
{
    gchar *category;			/* Current category */
    gboolean dirty;			/* Have changes been made? */
    gboolean minimized;
    gboolean locked;
    gboolean new_entry;
    gboolean startup_tray;		/* FPM2 started in tray */
} fpm_state;

typedef struct fpm_gui_struct
{
	GtkWidget *main_window;		/* The main window for the application   */
	GtkWidget *edit_window;		/* The edit window for a password item   */
	GtkWidget *pass_window;		/* Window for input master password      */
	GtkTreeView *main_clist;	/* Password CList on main window         */
	GtkMenu *toolbar_menu;
	GtkMenu *context_menu;
	GtkMenu *tray_menu;
	GtkStatusIcon *tray_icon;
} fpm_gui;

typedef enum
{
	BLOWFISH,
	AES256
} fpm_cipher_algo;

typedef struct fpm_cipher_struct
{
	gchar *name;
	gint hash_len;
	gint salt_len;
	size_t keylen;
	size_t blocksize;
	size_t contextsize;
} fpm_cipher;

typedef struct fpm_clipboard_struct {
	GdkAtom selection;
	gchar *user;
	gchar *password;
	gboolean is_password;
	gboolean multi_select;
	gboolean no_clear;
} fpm_clipboard;

fpm_ini *ini;					/* Store FPM settings */
fpm_gui *gui;					/* Store important GUI widget */
fpm_state *state;				/* Store running flags and other info */
fpm_cipher *cipher;				/* Store cipher infos */

//extern GtkTreeView* glb_clist_main; /* Password CList on main window         */

extern GtkWidget* glb_win_misc;  /* Misc window, used for dialogs         */

extern GtkWidget* glb_win_import; /* The import window                    */
extern fpm_data* glb_edit_data;  /* The password item being edited now    */
extern gint glb_num_row;	 /* The total number of rows in the CList */
extern gint glb_cur_row;	 /* The last item clicked on in the CList */
extern gchar* glb_filename;	 /* The location of the pasword file.     */
extern GTimer* glb_timer_click;  /* Timer used to check for double clicks */
extern gint glb_click_count;     /* Click count used for double clicks    */
extern gboolean glb_click_btn;   /* Right button clicked -> context menu  */
extern gint glb_click_row;       /* Store row for first click on dblclick */

extern gchar* new_salt;          /* Salt to use on next save.             */
extern gchar* old_salt;          /* Salt to used on last save.            */

extern void* old_context;
extern void* new_context;

extern char* vstring;
extern char* file_version;

extern fpm_cipher_algo cipher_algo;         /* Current cipher algorithm */

extern GList *glb_pass_list;     /* A GList of fpm_data containing all items */

extern GList *glb_cat_string_list;     /* A GList of strings, used to populate combos */
extern GList *glb_launcher_list; /* A list of ways we can launch a password */
extern GList *glb_launcher_string_list;     /* A GList of strings, used to populate combos */

extern gboolean glb_need_decrypt;

extern fpm_launcher* current_launcher;

void
fpm_new_passitem(GtkWidget** win_edit_ptr, fpm_data** data_ptr);  

void
fpm_edit_passitem(GtkWidget** win_edit_ptr, fpm_data* data);

int
fpm_save_passitem(GtkWidget* win_edit, fpm_data* data);

char*
fpm_get_entry(GtkWidget* win, gchar* entry_name);

void
fpm_set_entry(GtkWidget* win, gchar* entry_name, char* text);

void
fpm_quit(void);

void
fpm_init(char* opt_file_name, int tray_on_startup);

void
fpm_double_click(fpm_data* data);

void
fpm_jump(fpm_data* data);

void
fpm_check_password(void);

void
fpm_set_password(void);

void
fpm_clear_list(void);

void
fpm_clist_create_view(gchar* category); /* In fpm_clist.c */

void
fpm_clist_populate_cat_list(void);

void
fpm_clist_init(void);

void
fpm_create_category_list(gint edit_flag);

gchar*
fpm_create_cmd_line(gchar* cmd, gchar* arg, gchar* user, gchar* pass);

void fpm_init_launchers(void);

void fpm_create_launcher_string_list(void);

void fpm_launcher_init(void);

void fpm_prefs_save_launcher(void);

void fpm_debug(gchar* msg);

void lock_fpm_file(gchar* glb_filename);

void unlock_fpm_file1(void);

void fpm_message(GtkWindow* win, gchar* message, GtkMessageType message_type);

gint fpm_question(GtkWindow* win, gchar* message);

void fpm_ini_load(void);

void fpm_ini_save(void);

void fpm_search(gchar *search_text, gboolean select_first);

void fpm_execute_shell(gchar *cmd);

void fpm_statusbar_push(gchar *message);

void fpm_tray_icon();

gboolean fpm_auto_hide();

void fpm_tr_toggle_main_window(gboolean force_hide);

void fpm_tr_cleanup();

gboolean fpm_lock();

gboolean fpm_window_check();

void fpm_toolbar_style();

void fpm_clipboard_set(fpm_clipboard *clipboard);

char *fpm_get_combo_entry(GtkWidget *win, gchar *entry_name);

gboolean fpm_valid_edit_data();

gboolean fpm_is_launcher_in_category(fpm_launcher *launcher, gchar *category);

void on_clist_main_columns_changed          (GtkTreeView     *treeview,
                                    	     gpointer         user_data);

void fpm_view_modify_column(gint type, gboolean active);

void fpm_clist_set_data(fpm_data *data, GtkTreeModel *list_store, GtkTreeIter iter);

void fpm_middle_dblclick(fpm_data *data);

void fpm_clipboard_init(fpm_data *data, gint selection, gboolean is_password, gboolean multi_select);

void fpm_populate_combo_box(GtkComboBox *combo_box, gchar *active_item);

void fpm_copy_target_combo(GtkComboBox *combo_box, gint selection);

void fpm_unlock();

void fpm_switch_view_category(gchar *category);

void fpm_launcher_add();

void fpm_launcher_remove(fpm_launcher *launcher);

void fpm_launcher_remove_all();

void fpm_launcher_show(fpm_launcher *launcher);

void fpm_launcher_populate_combo_box(gint launcher_idx);

gboolean fpm_launcher_search(gchar *title);

void unlock_fpm_file(void);

void fpm_dirty(gboolean active);

void fpm_sensitive_menu(gboolean active);
