#pragma once

#include <stdlib.h>
#include <stdint.h>

// gtypes.h

typedef char   gchar;
typedef short  gshort;
typedef long   glong;
typedef int    gint;
typedef gint   gboolean;

typedef unsigned char   guchar;
typedef unsigned short  gushort;
typedef unsigned long   gulong;
typedef unsigned int    guint;

typedef float   gfloat;
typedef double  gdouble;

// glibconfig.h

typedef int8_t gint8;
typedef uint8_t guint8;
typedef int16_t gint16;
typedef uint16_t guint16;
typedef int32_t gint32;
typedef uint32_t guint32;
typedef int64_t gint64;
typedef uint64_t guint64;
typedef gint32 gssize;
typedef guint32 gsize;
typedef gint64 goffset;
typedef void * gpointer;

//typedef signed long gintptr;
//typedef unsigned long guintptr;

// gtk widgets (some fpm.h doesn't complain)

typedef uint32_t GList;
typedef uint32_t GtkWidget;
typedef uint32_t GtkTreeView;
typedef uint32_t GtkMenu;
typedef uint32_t GtkStatusIcon;
typedef uint32_t GdkAtom;
typedef uint32_t GTimer;
typedef uint32_t GtkWindow;
typedef uint32_t GtkMessageType;
typedef uint32_t GtkTreeIter;
typedef uint32_t GtkTreeModel;
typedef uint32_t GtkComboBox;

#define g_malloc(sz) malloc(sz)
#define g_free(ptr) free(ptr)

#define FALSE 0
#define TRUE 1

// TODO g_strdup_printf(format, args...)
// TODO g_malloc0(sz)
